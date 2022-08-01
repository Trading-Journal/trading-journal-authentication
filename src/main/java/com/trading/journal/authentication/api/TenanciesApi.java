package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.tenancy.Tenancy;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Tenancies Api")
@RequestMapping("/admin/tenancies")
public interface TenanciesApi {

    @ApiOperation(notes = "Get all", value = "Get all")
    @ApiResponses(@ApiResponse(code = 200, message = "Records retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<Tenancy>> getAll(
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @ApiParam(name = "sort", value = "A array with property and direction such as \"id,asc\", \"name,desc\"") @RequestParam(value = "sort", required = false) String[] sort,
            @RequestParam(value = "filter", required = false) String filter);

    @ApiOperation(notes = "Get by id", value = "Get record by its id")
    @ApiResponses(@ApiResponse(code = 200, message = "Record retrieved"))
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> getById(@PathVariable Long id);

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

    @ApiOperation(notes = "Get Tenancy by user email", value = "Get Tenancy by user email")
    @ApiResponses(@ApiResponse(code = 200, message = "Tenancy retrieved"))
    @GetMapping("/by-email/{email}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> getByEmail(@PathVariable String email);

    @ApiOperation(notes = "Delete tenancy by id", value = "Delete tenancy by id only if there is not user there")
    @ApiResponses(@ApiResponse(code = 200, message = "Tenancy Deleted"))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable Long id);
}
