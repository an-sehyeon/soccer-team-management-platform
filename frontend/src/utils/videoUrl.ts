// 백엔드 경기 영상 URL을 브라우저 재생 가능한 URL로 변환하는 파일

export function createVideoSourceUrl(videoUrl: string | null | undefined) {
  if (!videoUrl) {
    return "";
  }

  if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
    return videoUrl;
  }

  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

  const normalizedBaseUrl = apiBaseUrl.endsWith("/")
    ? apiBaseUrl.slice(0, -1)
    : apiBaseUrl;

  const normalizedVideoUrl = videoUrl.startsWith("/")
    ? videoUrl
    : `/${videoUrl}`;

  return `${normalizedBaseUrl}${normalizedVideoUrl}`;
}
