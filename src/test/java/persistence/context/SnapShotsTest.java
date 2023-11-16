package persistence.context;

import entity.SampleOneWithValidAnnotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Nested
@DisplayName("SnapShots 클래스의")
class SnapShotsTest {
    @Nested
    @DisplayName("getSnapShot 메소드는")
    class getSnapShot {
        @Nested
        @DisplayName("스냅샷에 존재하는 클래스타입과 아이디를 받으면")
        class withValidArgs {
            @Test
            @DisplayName("스냅샷을 반환한다.")
            void returnSnapShot() {
                //given
                SnapShots snapShots = new SnapShots();
                SampleOneWithValidAnnotation sample
                        = new SampleOneWithValidAnnotation(1L, "민준", 29);

                snapShots.putSnapShot(sample, "1");

                //when
                Object snapShot = snapShots.getSnapShotOrNull(SampleOneWithValidAnnotation.class, "1");

                //then
                assertThat(snapShot.toString()).isEqualTo("SampleOneWithValidAnnotation{id=1, name='민준', age=29}");
            }
        }

        @Nested
        @DisplayName("스냅샷에 존재하지않는 클래스타입과 아이디를 받으면")
        class withNotValidArgs {
            @Test
            @DisplayName("null을 반환한다.")
            void returnNull() {
                //given
                SnapShots snapShots = new SnapShots();

                //when
                Object snapShot = snapShots.getSnapShotOrNull(SampleOneWithValidAnnotation.class, "1");

                //then
                assertThat(snapShot).isNull();
            }
        }
    }

    @Nested
    @DisplayName("putSnapShot 메소드는")
    class putSnapShot {
        @Nested
        @DisplayName("인스턴스와 이이디를 받으면")
        class withValidArgs {
            @Test
            @DisplayName("스냅샷에 인스턴스의 복사본을 저장한고 반환한다.")
            void putSnapShot() {
                //given
                SnapShots snapShots = new SnapShots();

                SampleOneWithValidAnnotation sample
                        = new SampleOneWithValidAnnotation(1L, "민준", 29);

                //when
                //then
                assertThat(snapShots.putSnapShot(sample, "1").toString())
                        .isEqualTo("SampleOneWithValidAnnotation{id=1, name='민준', age=29}");
            }
        }
    }

    @Nested
    @DisplayName("remove 메소드는")
    class remove {
        @Nested
        @DisplayName("인스턴스와 이이디를 받으면")
        class withValidArgs {
            @Test
            @DisplayName("일차캐시에 인스턴스를 제거한다.")
            void putFirstCache() {
                //given
                FirstCaches firstCaches = new FirstCaches();
                SampleOneWithValidAnnotation sample
                        = new SampleOneWithValidAnnotation(1L, "민준", 29);

                firstCaches.putFirstCache(sample, "1");

                //when
                //then
                Assertions.assertDoesNotThrow(() ->
                        firstCaches.remove(SampleOneWithValidAnnotation.class, "1"));
            }
        }
    }
}