package com.manager.taskmanager.global.batch.task;

import com.manager.taskmanager.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OldTaskDeleteWriter implements ItemWriter<Long> {

    private final TaskRepository taskRepository;

    @Override
    public void write(Chunk<? extends Long> chunk) {
        List<Long> taskIds = new ArrayList<>(chunk.getItems());

        if (!taskIds.isEmpty()) {
            taskRepository.deleteAllByIdInBatch(taskIds);
        }
    }
}
