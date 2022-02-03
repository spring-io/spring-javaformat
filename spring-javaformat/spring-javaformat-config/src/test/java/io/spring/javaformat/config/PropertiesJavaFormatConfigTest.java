package io.spring.javaformat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PropertiesJavaFormatConfigTest {

    private JavaFormatConfig javaFormatConfig;

    @Mock
    private Properties properties;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        javaFormatConfig = new PropertiesJavaFormatConfig(properties);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getJavaBaselineWhenNullShouldReturnDefaultJavaBaseline() {
        when(properties.get(anyString()))
            .thenReturn(null);

        JavaBaseline actual = javaFormatConfig.getJavaBaseline();

        assertThat(actual).isEqualTo(JavaBaseline.V11);
    }

    @Test
    void getJavaBaselineWhenSetAs8ShouldReturnJavaBaselineV8() {
        when(properties.get(anyString()))
            .thenReturn(8);

        JavaBaseline actual = javaFormatConfig.getJavaBaseline();

        assertThat(actual).isEqualTo(JavaBaseline.V8);
    }

    @Test
    void getJavaBaselineWhenUnrecognisedVersionShouldThrowException() {
        when(properties.get(anyString()))
            .thenReturn("Unkown");

        assertThatThrownBy(() -> javaFormatConfig.getJavaBaseline())
            .hasNoCause()
            .hasMessage("No enum constant io.spring.javaformat.config.JavaBaseline.VUNKOWN");
    }
}
