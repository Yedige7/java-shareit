package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_validRequest_callsServiceAndReturns2xx() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful());

        verify(bookingService).create(1L, dto);
    }

    @Test
    void approve_callsServiceAndReturns2xx() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", 5L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().is2xxSuccessful());

        verify(bookingService).approve(2L, 5L, true);
    }

    @Test
    void getById_callsServiceAndReturns2xx() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", 10L)
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().is2xxSuccessful());

        verify(bookingService).getById(3L, 10L);
    }

    @Test
    void getForUser_validState_callsService() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().is2xxSuccessful());

        verify(bookingService).getBookingsForUser(1L, "ALL", 0, 10);
    }

    @Test
    void getForUser_unknownState_returns400_andDoesNotCallService() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNKNOWN")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getForOwner_validState_callsService() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().is2xxSuccessful());

        verify(bookingService).getBookingsForOwner(2L, "ALL", 5, 20);
    }

    @Test
    void getForOwner_unknownState_returns400_andDoesNotCallService() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "WRONG")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }
}
