import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ComponentsController } from './components/components.controller';
import { ComponentsService } from './components/components.service';
import { CacheModule, CacheInterceptor } from '@nestjs/cache-manager';
import { ConfigModule } from '@nestjs/config';
import { APP_INTERCEPTOR, APP_GUARD } from '@nestjs/core';
import { HttpModule } from '@nestjs/axios';
import { ThrottlerModule } from '@nestjs/throttler';
import { AuthGuard } from './auth/auth.guard';


@Module({
  imports: [
    HttpModule
    , CacheModule.register(
      {
        isGlobal: false,
        ttl: 0,
      }
    )
    , ConfigModule.forRoot(
      {
        isGlobal: true,
        cache: true,
      }),
    ThrottlerModule.forRoot({
      ttl: 300,
      limit: 1000,
    }),
  ],
  controllers: [
    AppController,
    ComponentsController,
  ],
  providers: [
    AppService,
    ComponentsService,

    {
      provide: APP_GUARD,
      useClass: AuthGuard,
    },
    {
      provide: APP_INTERCEPTOR,
      useClass: CacheInterceptor,
    },

  ],
})
export class AppModule { }
