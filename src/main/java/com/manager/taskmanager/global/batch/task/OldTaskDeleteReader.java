package com.manager.taskmanager.global.batch.task;

import com.manager.taskmanager.task.TaskQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OldTaskDeleteReader implements ItemReader<Long> {

    private final TaskQueryRepository taskQueryRepository;
    private Iterator<Long> idIterator;

    @Override
    public Long read() throws Exception {
        if (idIterator == null) {
            LocalDateTime dateTime = LocalDateTime.now().minusMonths(3);

            List<Long> taskIds = taskQueryRepository.getDeletedTaskAfter3Month(dateTime);
            idIterator = taskIds.iterator();
        }

        return idIterator.hasNext() ? idIterator.next() : null;
    }
}
