import { CallHandler, ExecutionContext, NestInterceptor, Injectable } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { Observable } from 'rxjs';

@Injectable()
export class HttpServiceInterceptor implements NestInterceptor {
  constructor(private httpService: HttpService) {}
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
  
    // ** if you use normal HTTP module **
    const ctx = context.switchToHttp();
    const token = ctx.getRequest().headers['authorization'];
    console.log(ctx.getRequest().headers);
    if (token) {
      console.log("token" + token)
      this.httpService.axiosRef.defaults.headers.common['authorization'] =
        token;
    }
    else {
      console.log("no token" + token);
      delete this.httpService.axiosRef.defaults.headers.common["authorization"];
    }
    return next.handle().pipe();
  }
}