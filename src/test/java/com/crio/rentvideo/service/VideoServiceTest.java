package com.crio.rentvideo.service;

import com.crio.rentvideo.dto.VideoRequest;
import com.crio.rentvideo.entity.Video;
import com.crio.rentvideo.exception.ResourceNotFoundException;
import com.crio.rentvideo.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    private Video sampleVideo;

    @BeforeEach
    void setUp() {
        sampleVideo = new Video("Rental Video Alpha", "Director A", "Documentary", true);
        sampleVideo.setId(10L);
    }

    @Test
    void testCreateVideo_DefaultsToAvailable() {
        VideoRequest request = new VideoRequest("Rental Video Beta", "Director B", "Drama", null);

        Video created = new Video("Rental Video Beta", "Director B", "Drama", true);
        created.setId(11L);

        when(videoRepository.save(any(Video.class))).thenReturn(created);

        Video result = videoService.createVideo(request);

        assertNotNull(result);
        assertEquals("Rental Video Beta", result.getTitle());
        assertTrue(result.getAvailabilityStatus());
    }

    @Test
    void testGetAllVideos() {
        when(videoRepository.findAll()).thenReturn(Arrays.asList(sampleVideo));

        List<Video> list = videoService.getAllVideos();
        assertEquals(1, list.size());
        assertEquals("Rental Video Alpha", list.get(0).getTitle());
    }

    @Test
    void testGetAvailableVideos() {
        when(videoRepository.findByAvailabilityStatus(true)).thenReturn(Arrays.asList(sampleVideo));

        List<Video> list = videoService.getAvailableVideos();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getAvailabilityStatus());
    }

    @Test
    void testGetVideoById_Found() {
        when(videoRepository.findById(10L)).thenReturn(Optional.of(sampleVideo));

        Video video = videoService.getVideoById(10L);
        assertNotNull(video);
        assertEquals("Rental Video Alpha", video.getTitle());
    }

    @Test
    void testGetVideoById_NotFoundThrowsException() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.getVideoById(99L));
    }

    @Test
    void testUpdateVideo_Success() {
        VideoRequest updateReq = new VideoRequest("Rental Video Alpha Updated", "Director A", "Documentary", false);
        when(videoRepository.findById(10L)).thenReturn(Optional.of(sampleVideo));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Video updated = videoService.updateVideo(10L, updateReq);

        assertEquals("Rental Video Alpha Updated", updated.getTitle());
        assertFalse(updated.getAvailabilityStatus());
    }

    @Test
    void testDeleteVideo_Success() {
        when(videoRepository.findById(10L)).thenReturn(Optional.of(sampleVideo));
        doNothing().when(videoRepository).delete(sampleVideo);

        assertDoesNotThrow(() -> videoService.deleteVideo(10L));
        verify(videoRepository, times(1)).delete(sampleVideo);
    }
}
