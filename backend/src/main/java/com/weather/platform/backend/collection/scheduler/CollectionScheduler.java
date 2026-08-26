package com.weather.platform.backend.collection.scheduler;

import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.collection.entity.TriggerType;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.collection.service.CollectionExecutionService;
import com.weather.platform.backend.collection.service.ScheduleIntervalCalculator;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CollectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(CollectionScheduler.class);
    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");

    private final CollectionTargetRepository collectionTargetRepository;
    private final CollectionJobRepository collectionJobRepository;
    private final CollectionExecutionService collectionExecutionService;

    public CollectionScheduler(CollectionTargetRepository collectionTargetRepository,
                                CollectionJobRepository collectionJobRepository,
                                CollectionExecutionService collectionExecutionService) {
        this.collectionTargetRepository = collectionTargetRepository;
        this.collectionJobRepository = collectionJobRepository;
        this.collectionExecutionService = collectionExecutionService;
    }

    @Scheduled(fixedRate = 60_000)
    public void run() {
        List<CollectionTarget> targets = collectionTargetRepository.findByEnabledTrue();

        for (CollectionTarget target : targets) {
            if (!isDue(target)) {
                continue;
            }
            try {
                collectionExecutionService.execute(target.getTargetId(), null, TriggerType.SCHEDULED);
            } catch (Exception e) {
                log.warn("스케줄 수집 실행 실패: targetId={}, dataCode={}", target.getTargetId(), target.getDataCode(), e);
            }
        }
    }

    private boolean isDue(CollectionTarget target) {
        Optional<CollectionJob> lastJob =
                collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(target.getTargetId());
        if (lastJob.isEmpty()) {
            return true;
        }

        Duration interval = ScheduleIntervalCalculator.toDuration(target.getScheduleType(), target.getIntervalValue());
        OffsetDateTime nextDue = lastJob.get().getStartedAt().plus(interval);
        return !OffsetDateTime.now(SEOUL_OFFSET).isBefore(nextDue);
    }
}
