import com.cn.ridge.config.AccessConfig;
import com.cn.ridge.config.QuartzSchedulerConfig;
import com.cn.ridge.startup.RunCycleJob;
import com.cn.ridge.startup.RunSingleJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.cn.ridge")
@EnableAutoConfiguration
@EnableFeignClients
@Import({AccessConfig.class,
        QuartzSchedulerConfig.class,
        RunCycleJob.class,
        RunSingleJob.class
})
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
