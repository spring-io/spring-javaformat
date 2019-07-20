import * as child_process from 'child_process'
import * as portfinder from 'portfinder'
import psList from 'ps-list'
import axios from 'axios'
import { resolve } from 'path'
import { window } from 'vscode'

const JAR_NAME = 'spring-javaformat-format-service-0.0.16-SNAPSHOT.jar'

const RUNTIME_JAR_PATH = resolve(__dirname, '..', 'runtime', JAR_NAME)

export default class FormatService {
  private static instance: FormatService = new FormatService()
  private port: number = 0

  constructor() {
    const hideFunc = window.setStatusBarMessage('spring-javaformat service initializing....')
    this.init()
      .catch(err => {
        console.error('ERROR:', err)
      })
      .finally(() => {
        hideFunc.dispose()
      })
  }

  private async init() {
    const proc = await this.getJavaFormatServiceProcessInfo()
    if (proc) {
      const matched = (proc.cmd || '').match(/-Dport=([0-9]+)\s/)
      if (matched) {
        this.port = +matched[1]
      }
      console.log('spring-javaformat service is running with other workspace, no need run again')
      return
    }
    const port = await this.findAvailablePort()
    this.port = port
    await this.run(port)
    console.log('spring-javaformat service running')
  }

  private async getJavaFormatServiceProcessInfo() {
    const list = await psList()
    return list.find(l => (l.cmd || '').includes(JAR_NAME))
  }

  private async findAvailablePort() {
    const port = await portfinder.getPortPromise({
      port: 20000,
      stopPort: 60000
    })
    return port
  }

  private async run(port: number) {
    return new Promise((resolve, reject) => {
      const startTimer = setTimeout(() => {
        resolve()
      }, 1000 * 40)

      child_process.exec(`java -Dport=${port} -jar ${RUNTIME_JAR_PATH} `, {}, (error, stdout, stderr) => {
        if (error || stderr) {
          clearTimeout(startTimer)
          return reject(error || stderr)
        }
      })
    })
  }

  async formatFile(filePath: string): Promise<string> {
    try {
      const result = await axios.post(`http://localhost:${this.port}/format`, {
        filePath
      })
      return result.data
    } catch (error) {
      throw new Error('spring-javaformat service is not ready, please hold for few seconds')
    }
  }

  static getInstance(): FormatService {
    return this.instance
  }
}
