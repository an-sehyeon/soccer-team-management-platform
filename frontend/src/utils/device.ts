// 현재 기기가 모바일/태블릿 공통 화면 대상인지 판단하는 파일

// 휴대폰은 화면 너비 기준으로 모바일 화면 처리
const MOBILE_MAX_WIDTH = 768;

// 태블릿은 터치 지원 + 화면 너비 기준으로 모바일 공통 화면 처리
const TABLET_MAX_WIDTH = 1024;

// 모바일 또는 태블릿 여부 확인
export function isMobileOrTablet() {
  const width = window.innerWidth;
  const hasTouchScreen = navigator.maxTouchPoints > 0;

  if (width <= MOBILE_MAX_WIDTH) {
    return true;
  }

  if (hasTouchScreen && width <= TABLET_MAX_WIDTH) {
    return true;
  }

  return false;
}
