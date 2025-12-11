package com.tetgame.modules.room.task;

import com.tetgame.modules.room.entity.Room;
import com.tetgame.modules.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomCleanupTask {

    private final RoomRepository roomRepository;

    // Run every 5 minutes
    @Scheduled(fixedDelayString = "PT5M")
    public void cleanupInactiveRooms() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<Room> old = roomRepository.findFinishedRoomsBefore(cutoff);
        for (Room r : old) {
            try {
                roomRepository.delete(r);
            } catch (Exception ex) {
                // ignore
            }
        }
    }
}
