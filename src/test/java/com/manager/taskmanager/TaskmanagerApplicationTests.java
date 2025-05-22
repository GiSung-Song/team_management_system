package com.manager.taskmanager;

import com.manager.taskmanager.config.DBContainerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(DBContainerExtension.class)
class TaskmanagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
