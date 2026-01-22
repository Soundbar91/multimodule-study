package com.soundbar91.shop.api.controller;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.shop.api.dto.request.CreateShopRequest;
import com.soundbar91.shop.api.dto.request.UpdateShopRequest;
import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;
import com.soundbar91.shop.service.ShopService;
import com.soundbar91.test.fixture.ShopFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShopControllerTest.TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ShopController 테스트")
class ShopControllerTest {

    @EnableAutoConfiguration
    @ComponentScan(basePackages = "com.soundbar91.shop.api")
    static class TestConfig {

        @RestControllerAdvice
        static class TestExceptionHandler {
            @ExceptionHandler(NotFoundException.class)
            public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShopService shopService;

    @Nested
    @DisplayName("POST /api/v2/shops")
    class CreateShop {

        @Test
        @DisplayName("유효한 요청으로 상점을 생성하면 201 Created를 반환한다")
        void createShop_WithValidRequest_Returns201() throws Exception {
            // given
            CreateShopRequest request = new CreateShopRequest(
                    "테스트 상점", ShopCategory.RESTAURANT, "맛있는 음식점",
                    "서울시 강남구", "02-1234-5678", 1L);

            Shop createdShop = ShopFixture.create()
                    .withId(1L)
                    .withName("테스트 상점")
                    .withCategory(ShopCategory.RESTAURANT)
                    .withOwnerId(1L)
                    .build();

            given(shopService.createShop(any(), any(), any(), any(), any(), any())).willReturn(createdShop);

            // when & then
            mockMvc.perform(post("/api/v2/shops")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 상점"))
                    .andExpect(jsonPath("$.category").value("RESTAURANT"));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/shops/{id}")
    class GetShop {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200 OK와 상점 정보를 반환한다")
        void getShop_WithExistingId_Returns200() throws Exception {
            // given
            Long shopId = 1L;
            Shop shop = ShopFixture.create()
                    .withId(shopId)
                    .withName("테스트 상점")
                    .build();

            given(shopService.getShopById(shopId)).willReturn(shop);

            // when & then
            mockMvc.perform(get("/api/v2/shops/{id}", shopId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shopId))
                    .andExpect(jsonPath("$.name").value("테스트 상점"));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404 Not Found를 반환한다")
        void getShop_WithNonExistingId_Returns404() throws Exception {
            // given
            Long shopId = 999L;
            given(shopService.getShopById(shopId))
                    .willThrow(new NotFoundException("상점을 찾을 수 없습니다. ID: " + shopId));

            // when & then
            mockMvc.perform(get("/api/v2/shops/{id}", shopId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/shops")
    class GetAllShops {

        @Test
        @DisplayName("모든 상점을 조회하면 200 OK와 상점 목록을 반환한다")
        void getAllShops_Returns200WithList() throws Exception {
            // given
            List<Shop> shops = List.of(
                    ShopFixture.create().withId(1L).withName("상점1").build(),
                    ShopFixture.create().withId(2L).withName("상점2").build()
            );

            given(shopService.getAllShops()).willReturn(shops);

            // when & then
            mockMvc.perform(get("/api/v2/shops"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/shops/category/{category}")
    class GetShopsByCategory {

        @Test
        @DisplayName("카테고리별 상점을 조회하면 해당 카테고리의 상점 목록을 반환한다")
        void getShopsByCategory_Returns200WithList() throws Exception {
            // given
            List<Shop> cafes = List.of(
                    ShopFixture.create().withId(1L).withCategory(ShopCategory.CAFE).build(),
                    ShopFixture.create().withId(2L).withCategory(ShopCategory.CAFE).build()
            );

            given(shopService.getShopsByCategory(ShopCategory.CAFE)).willReturn(cafes);

            // when & then
            mockMvc.perform(get("/api/v2/shops/category/{category}", "CAFE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("PUT /api/v2/shops/{id}")
    class UpdateShop {

        @Test
        @DisplayName("유효한 요청으로 상점을 수정하면 200 OK를 반환한다")
        void updateShop_WithValidRequest_Returns200() throws Exception {
            // given
            Long shopId = 1L;
            UpdateShopRequest request = new UpdateShopRequest(
                    "수정된 상점", "수정된 설명", "새로운 주소", "02-9999-8888");

            Shop updatedShop = ShopFixture.create()
                    .withId(shopId)
                    .withName("수정된 상점")
                    .withDescription("수정된 설명")
                    .build();

            given(shopService.updateShopInfo(eq(shopId), any(), any(), any(), any())).willReturn(updatedShop);

            // when & then
            mockMvc.perform(put("/api/v2/shops/{id}", shopId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된 상점"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/shops/{id}/activate")
    class ActivateShop {

        @Test
        @DisplayName("상점을 활성화하면 200 OK를 반환한다")
        void activateShop_Returns200() throws Exception {
            // given
            Long shopId = 1L;
            Shop activatedShop = ShopFixture.create()
                    .withId(shopId)
                    .build();

            given(shopService.activateShop(shopId)).willReturn(activatedShop);

            // when & then
            mockMvc.perform(patch("/api/v2/shops/{id}/activate", shopId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/shops/{id}/deactivate")
    class DeactivateShop {

        @Test
        @DisplayName("상점을 비활성화하면 200 OK를 반환한다")
        void deactivateShop_Returns200() throws Exception {
            // given
            Long shopId = 1L;
            Shop deactivatedShop = ShopFixture.create()
                    .withId(shopId)
                    .withIsActive(false)
                    .build();

            given(shopService.deactivateShop(shopId)).willReturn(deactivatedShop);

            // when & then
            mockMvc.perform(patch("/api/v2/shops/{id}/deactivate", shopId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/shops/{id}")
    class DeleteShop {

        @Test
        @DisplayName("존재하는 상점을 삭제하면 204 No Content를 반환한다")
        void deleteShop_WithExistingShop_Returns204() throws Exception {
            // given
            Long shopId = 1L;
            willDoNothing().given(shopService).deleteShop(shopId);

            // when & then
            mockMvc.perform(delete("/api/v2/shops/{id}", shopId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 상점을 삭제하면 404 Not Found를 반환한다")
        void deleteShop_WithNonExistingShop_Returns404() throws Exception {
            // given
            Long shopId = 999L;
            willThrow(new NotFoundException("상점을 찾을 수 없습니다"))
                    .given(shopService).deleteShop(shopId);

            // when & then
            mockMvc.perform(delete("/api/v2/shops/{id}", shopId))
                    .andExpect(status().isNotFound());
        }
    }
}
