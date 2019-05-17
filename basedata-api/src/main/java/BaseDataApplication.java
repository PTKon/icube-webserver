import com.cn.ridge.config.AccessConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.cn.ridge")
@EnableAutoConfiguration
@Import({AccessConfig.class})
public class BaseDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseDataApplication.class, args);
    }
}
