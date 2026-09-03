package cn.artoria.mind_forge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

@SpringBootApplication
@RestController
public class MindForgeApplication {

	private final PasswordEncoder passwordEncoder;

	public MindForgeApplication(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}


    public static void main(String[] args) {
        SpringApplication.run(MindForgeApplication.class, args);
    }

    // 隐藏这个接口
    @Hidden
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
		// passwordEncoder.encode("123456")
	}

}
