export const environment = {
  production: false,
  loginPath: 'login',
  logoutPath: 'logout',
  statePath: 'state',
  stompBrokerUrl: ((window.location.protocol.indexOf('https') !== -1) ? 'wss' : 'ws') + '://' + window.location.hostname +
    ((!!window.location.port) ? (':' + (window.location.port)) : '') + '/stomp/messages'
};
