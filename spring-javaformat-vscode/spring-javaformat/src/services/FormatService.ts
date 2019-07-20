import * as child_process from 'child_process'
import * as portfinder from 'portfinder'
import psList from 'ps-list'
import axios from 'axios'
import { resolve } from 'path'
import { window } from 'vscode'

const JAR_NAME = 'spring-javaformat-format-service-0.0.16-SNAPSHOT.jar'

const RUNTIME_JAR_PATH = resolve(__dirname, '..', '..', 'runtime', JAR_NAME)

export default class FormatService {
  private static instance: FormatService = new FormatService()
  private port: number = 9987

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
    this.startNextScheduledJob()
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

  private startNextScheduledJob() {
    setTimeout(async () => {
      const proc = await this.getJavaFormatServiceProcessInfo()
      if (!proc) {
        const port = await this.findAvailablePort()
        this.port = port
        await this.run(port)
      }
      await this.sendHeartbeat()

      this.startNextScheduledJob()
    }, 1000 * 60)
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
      const childProcess = child_process.exec(`java -Dport=${port} -jar ${RUNTIME_JAR_PATH} `, {})

      childProcess.stdout.on('data', data => {
        if (data.includes('Started FormatterWebApplication')) {
          resolve()
        }
      })

      childProcess.on('error', reject)
    })
  }

  async formatCode(source: string): Promise<string> {
    try {
      const result = await axios.post(`http://localhost:${this.port}/format/code`, {
        source
      })
      return result.data
    } catch (error) {
      throw new Error('spring-javaformat service is not ready, please hold for few seconds')
    }
  }

  async sendHeartbeat() {
    return axios.get(`http://localhost:${this.port}/health`)
  }

  static getInstance(): FormatService {
    return this.instance
  }
}
