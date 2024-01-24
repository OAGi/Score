import { Controller, UseGuards } from '@nestjs/common';
import {  Get, Req } from '@nestjs/common';
import { BieService } from './bie.service';
import { Param, Query } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { CacheTTL } from '@nestjs/cache-manager';
import { AuthGuard } from 'src/auth/auth.guard';

@Controller('api/bie')
@UseGuards(AuthGuard)
export class BieController {

  constructor(private readonly bieService: BieService, private configService: ConfigService) { }

  @Get()
  findAll(
    @Query('branch') branch,
    @Query('den') den,
    @Query('businessContext') businessContext,
    @Query('states') states
  ) {
    return this.bieService.getAllBieMetadata(branch,businessContext,den,states);
  }


  @Get('uuid/:uuid')
  findBie(@Param('uuid') uuid: string
  )  {
      return this.bieService.getBieSchema(uuid);
  }

 
}

