export type OverviewActiveState = {
  introductionActive: boolean;
  gettingStartedActive: boolean;
  authenticationActive: boolean;
};

export function getOverviewActiveState(pathname: string, hash: string): OverviewActiveState {
  const onOverviewPage = pathname === '/overview';
  if (!onOverviewPage) {
    return {
      introductionActive: false,
      gettingStartedActive: false,
      authenticationActive: false,
    };
  }

  const normalizedHash = hash || '';
  if (normalizedHash === '#authentication') {
    return {
      introductionActive: false,
      gettingStartedActive: false,
      authenticationActive: true,
    };
  }
  if (normalizedHash === '#getting-started') {
    return {
      introductionActive: false,
      gettingStartedActive: true,
      authenticationActive: false,
    };
  }
  return {
    introductionActive: true,
    gettingStartedActive: false,
    authenticationActive: false,
  };
}
