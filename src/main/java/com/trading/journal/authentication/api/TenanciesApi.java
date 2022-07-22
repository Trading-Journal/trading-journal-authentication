package com.trading.journal.authentication.api;

import com.trading.journal.authentication.tenancy.Tenancy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Api(tags = "Tenancies Api")
@RequestMapping("/admin/tenancies")
public interface TenanciesApi extends PageableApi<Tenancy> {

    @ApiOperation(notes = "Disable tenancy", value = "Disable tenancy")
    @ApiResponses(@ApiResponse(code = 200, message = "Tenancy disabled"))
    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(@PathVariable Long id);

    @ApiOperation(notes = "Enable tenancy", value = "Enable tenancy")
    @ApiResponses(@ApiResponse(code = 200, message = "Tenancy enabled"))
    @PatchMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(@PathVariable Long id);

    @ApiOperation(notes = "Set tenancy limits", value = "Set tenancy limit")
    @ApiResponses(@ApiResponse(code = 200, message = "Tenancy limit updated"))
    @PatchMapping("/{id}/limit/{limit}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> limit(@PathVariable Long id, @PathVariable Integer limit);
}
