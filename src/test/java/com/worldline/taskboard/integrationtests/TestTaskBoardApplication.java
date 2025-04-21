package com.worldline.taskboard.integrationtests;

import com.worldline.taskboard.TaskBoardApplication;
import org.springframework.boot.SpringApplication;

public class TestTaskBoardApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskBoardApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
