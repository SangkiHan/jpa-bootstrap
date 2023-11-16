package persistence.entity.attribute.resolver;

import entity.SampleEntityWithIntegerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Nested
@DisplayName("IntegerTypeIdAttributeResolver 클래스의")
class IntegerTypeIdAttributeResolverTest {
    @Nested
    @DisplayName("supports 메소드는")
    class supports {
        @Nested
        @DisplayName("Integer가 주어지면")
        class withInteger {
            @Test
            @DisplayName("true를 반환한다")
            void supports() {
                //given
                //when
                IdAttributeResolver resolver = new IntegerTypeIdAttributeResolver();

                //then
                assertThat(resolver.supports(Integer.class)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("resolve 메소드는")
    class resolve {
        @Nested
        @DisplayName("Integer 타입의 필드가 주어지면")
        class withField {
            @Test
            @DisplayName("IntegerTypeIdAttribute를 반환한다")
            void returnAttribute() throws NoSuchFieldException {
                //given
                IdAttributeResolver resolver = new IntegerTypeIdAttributeResolver();
                SampleEntityWithIntegerId sample
                        = new SampleEntityWithIntegerId(1, "민준", 29);
                Field field = sample.getClass().getDeclaredField("id");

                //when
                //then
                Assertions.assertDoesNotThrow(() -> resolver.resolve(field));
            }
        }
    }

    @Nested
    @DisplayName("setIdToEntity 메소드는")
    class setIdToEntity {
        @Nested
        @DisplayName("인스턴스와 이이디 필드 그리고 벨류가 주어지면")
        class withValidArgs {
            @Test
            @DisplayName("인스턴스 아이디 필드에 아이디 벨류를 세팅한다.")
            void setIdToEntity() throws NoSuchFieldException, IllegalAccessException {
                //given
                IdAttributeResolver resolver = new IntegerTypeIdAttributeResolver();
                SampleEntityWithIntegerId sample
                        = new SampleEntityWithIntegerId("민준", 29);

                Field field = sample.getClass().getDeclaredField("id");

                //when
                resolver.setIdToEntity(sample, field, 10);

                //then
                assertThat(field.get(sample)).isEqualTo(10);
            }
        }
    }
}