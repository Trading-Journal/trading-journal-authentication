package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class TenanciesController implements TenanciesApi {

    private final TenancyService tenancyService;

    @Override
    public ResponseEntity<PageResponse<Tenancy>> getAll(Integer page, Integer size, String[] sort, String filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filter(filter)
                .build();
        PageResponse<Tenancy> pageResponse = tenancyService.getAll(pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<Tenancy> getById(Long id) {
        return ok(tenancyService.getById(id));
    }

    @Override
    public ResponseEntity<Void> disable(Long id) {
        tenancyService.disable(id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> enable(Long id) {
        tenancyService.enable(id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Tenancy> limit(Long id, Integer limit) {
        return ok(tenancyService.newLimit(id, limit));
    }
}
