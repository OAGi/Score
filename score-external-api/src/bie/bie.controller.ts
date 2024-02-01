import { Controller, UseGuards } from '@nestjs/common';
import {  Get, Req } from '@nestjs/common';
import { BieService } from './bie.service';
import { Param, Query } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { CacheTTL } from '@nestjs/cache-manager';
import { AuthGuard } from 'src/auth/auth.guard';
import { ApiOperation, ApiResponse, ApiTags, ApiQuery, ApiBearerAuth } from '@nestjs/swagger';


@ApiTags('BIE')
@Controller('api/bie')
@UseGuards(AuthGuard)
export class BieController {

  constructor(private readonly bieService: BieService, private configService: ConfigService) { }

  @Get()
  @ApiQuery({ name: 'den', required: false, description: 'Filter by all BIEs whose DEN (Dictionary Entry Name) contains the search string', example: 'Asset' })
  @ApiQuery({ name: 'businessContext', required: false, description: 'Filter by business contexts', example: 'S-Series' })
  @ApiQuery({ name: 'states', required: false, description: 'Filter by specific BIE states', example: 'states=Production' })
  @ApiQuery({ name: 'branch', required: false, description: 'Filter by a specific branch of connectSpec (OAGIS)', example: '10.9.2' })
  @ApiResponse({ status: 200, description: 'Data retrieved'})
  @ApiResponse({ status: 401, description: 'Unauthorized'})
  @ApiOperation({ summary: 'Return information about BIEs.  BIEs in WIP state or lower are not returned'
  + '  If the release version parameter is not specified it will return the latest version.' })
  @ApiBearerAuth()
  @CacheTTL(1000000)
  findAll(
    @Query('den') den,
    @Query('businessContext') businessContext,
    @Query('states') states,
    @Query('branch') branch,
  ) {
    return this.bieService.getAllBieMetadata(branch,businessContext,den,states);
  }


  @Get('uuid/:uuid')
  @ApiOperation({ summary: 'Return BIE Schema for the given UUID -'
  + '  currently XSD is the only schema type supported.' })
  @ApiResponse({ status: 200, description: 'Data retrieved'})
  @ApiResponse({ status: 401, description: 'Unauthorized'})
  @ApiBearerAuth()
  findBie(@Param('uuid') uuid: string
  )  {
      return this.bieService.getBieSchema(uuid);
  }

 
}

