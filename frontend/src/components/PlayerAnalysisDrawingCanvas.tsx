// 선수 개인 분석 클립 영상 위에 드로잉 데이터를 캔버스로 표시하고 새 드로잉 좌표를 생성하는 컴포넌트

import { useEffect, useRef, useState } from "react";
import type { MouseEvent } from "react";
import type {
  PlayerAnalysisClipDrawingData,
  PlayerAnalysisClipDrawingResponse,
  PlayerAnalysisClipDrawingType,
} from "../types/playerAnalysisClipDrawing";

type Point = {
  x: number;
  y: number;
};

type PlayerAnalysisDrawingCanvasProps = {
  drawings: PlayerAnalysisClipDrawingResponse[];
  currentTimeSec: number;
  isDrawingMode: boolean;
  drawingType: PlayerAnalysisClipDrawingType;
  drawingText: string;
  onDraftDrawingData: (drawingData: PlayerAnalysisClipDrawingData) => void;
  onInvalidDraftMessage?: (message: string) => void;
};

const DEFAULT_COLOR = "#ff0000";
const DEFAULT_LINE_WIDTH = 4;
const DEFAULT_FONT_SIZE = 18;

export default function PlayerAnalysisDrawingCanvas({
  drawings,
  currentTimeSec,
  isDrawingMode,
  drawingType,
  drawingText,
  onDraftDrawingData,
  onInvalidDraftMessage,
}: PlayerAnalysisDrawingCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [dragStartPoint, setDragStartPoint] = useState<Point | null>(null);

  const resizeCanvas = () => {
    const canvas = canvasRef.current;
    const parent = canvas?.parentElement;

    if (!canvas || !parent) {
      return;
    }

    const rect = parent.getBoundingClientRect();

    if (rect.width <= 0 || rect.height <= 0) {
      return;
    }

    canvas.width = rect.width;
    canvas.height = rect.height;
  };

  const getMousePoint = (
    event: MouseEvent<HTMLCanvasElement>,
  ): Point | null => {
    const canvas = canvasRef.current;

    if (!canvas) {
      return null;
    }

    const rect = canvas.getBoundingClientRect();

    if (rect.width <= 0 || rect.height <= 0) {
      return null;
    }

    return {
      x: (event.clientX - rect.left) / rect.width,
      y: (event.clientY - rect.top) / rect.height,
    };
  };

  const handleMouseDown = (event: MouseEvent<HTMLCanvasElement>) => {
    if (!isDrawingMode) {
      return;
    }

    const point = getMousePoint(event);

    if (!point) {
      return;
    }

    if (drawingType === "TEXT") {
      const trimmedText = drawingText.trim();

      if (trimmedText === "") {
        onInvalidDraftMessage?.(
          "텍스트 드로잉은 내용을 입력한 뒤 영상 위를 클릭해주세요.",
        );
        return;
      }

      onDraftDrawingData({
        version: 1,
        x: point.x,
        y: point.y,
        text: trimmedText,
        color: DEFAULT_COLOR,
        fontSize: DEFAULT_FONT_SIZE,
      });

      return;
    }

    setDragStartPoint(point);
  };

  const handleMouseUp = (event: MouseEvent<HTMLCanvasElement>) => {
    if (!isDrawingMode || !dragStartPoint) {
      return;
    }

    const endPoint = getMousePoint(event);

    if (!endPoint) {
      setDragStartPoint(null);
      return;
    }

    const drawingData = createDrawingData(
      drawingType,
      dragStartPoint,
      endPoint,
    );

    onDraftDrawingData(drawingData);
    setDragStartPoint(null);
  };

  useEffect(() => {
    resizeCanvas();

    window.addEventListener("resize", resizeCanvas);

    return () => {
      window.removeEventListener("resize", resizeCanvas);
    };
  }, []);

  useEffect(() => {
    resizeCanvas();

    const canvas = canvasRef.current;

    if (!canvas) {
      return;
    }

    const context = canvas.getContext("2d");

    if (!context) {
      return;
    }

    context.clearRect(0, 0, canvas.width, canvas.height);

    drawings
      .filter(
        (drawing) =>
          drawing.startTimeSec <= currentTimeSec &&
          currentTimeSec <= drawing.endTimeSec,
      )
      .forEach((drawing) => {
        drawSavedDrawing(
          context,
          canvas.width,
          canvas.height,
          drawing.drawingType,
          drawing.drawingData,
        );
      });
  }, [drawings, currentTimeSec]);

  return (
    <canvas
      ref={canvasRef}
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
      style={{
        position: "absolute",
        inset: 0,
        width: "100%",
        height: "100%",
        zIndex: 10,
        display: "block",
        pointerEvents: isDrawingMode ? "auto" : "none",
      }}
      aria-label="선수 개인 분석 클립 드로잉 캔버스"
    />
  );
}

function createDrawingData(
  drawingType: PlayerAnalysisClipDrawingType,
  startPoint: Point,
  endPoint: Point,
): PlayerAnalysisClipDrawingData {
  if (drawingType === "LINE" || drawingType === "ARROW") {
    return {
      version: 1,
      fromX: startPoint.x,
      fromY: startPoint.y,
      toX: endPoint.x,
      toY: endPoint.y,
      color: DEFAULT_COLOR,
      lineWidth: DEFAULT_LINE_WIDTH,
    };
  }

  if (drawingType === "CIRCLE") {
    return {
      version: 1,
      x: (startPoint.x + endPoint.x) / 2,
      y: (startPoint.y + endPoint.y) / 2,
      radiusX: Math.abs(endPoint.x - startPoint.x) / 2,
      radiusY: Math.abs(endPoint.y - startPoint.y) / 2,
      color: DEFAULT_COLOR,
      lineWidth: DEFAULT_LINE_WIDTH,
    };
  }

  return {
    version: 1,
    x: Math.min(startPoint.x, endPoint.x),
    y: Math.min(startPoint.y, endPoint.y),
    width: Math.abs(endPoint.x - startPoint.x),
    height: Math.abs(endPoint.y - startPoint.y),
    color: DEFAULT_COLOR,
    lineWidth: DEFAULT_LINE_WIDTH,
  };
}

function drawSavedDrawing(
  context: CanvasRenderingContext2D,
  canvasWidth: number,
  canvasHeight: number,
  drawingType: PlayerAnalysisClipDrawingType,
  drawingData: PlayerAnalysisClipDrawingData,
) {
  if (drawingType === "LINE") {
    drawLine(context, canvasWidth, canvasHeight, drawingData, false);
    return;
  }

  if (drawingType === "ARROW") {
    drawLine(context, canvasWidth, canvasHeight, drawingData, true);
    return;
  }

  if (drawingType === "CIRCLE") {
    drawCircle(context, canvasWidth, canvasHeight, drawingData);
    return;
  }

  if (drawingType === "BOX" || drawingType === "AREA") {
    drawBox(context, canvasWidth, canvasHeight, drawingData, drawingType);
    return;
  }

  if (drawingType === "TEXT") {
    drawText(context, canvasWidth, canvasHeight, drawingData);
  }
}

function drawLine(
  context: CanvasRenderingContext2D,
  canvasWidth: number,
  canvasHeight: number,
  drawingData: PlayerAnalysisClipDrawingData,
  hasArrowHead: boolean,
) {
  const fromX = getNumber(drawingData.fromX) * canvasWidth;
  const fromY = getNumber(drawingData.fromY) * canvasHeight;
  const toX = getNumber(drawingData.toX) * canvasWidth;
  const toY = getNumber(drawingData.toY) * canvasHeight;

  context.beginPath();
  context.strokeStyle = getString(drawingData.color, DEFAULT_COLOR);
  context.lineWidth = getNumber(drawingData.lineWidth, DEFAULT_LINE_WIDTH);
  context.moveTo(fromX, fromY);
  context.lineTo(toX, toY);
  context.stroke();

  if (hasArrowHead) {
    drawArrowHead(context, fromX, fromY, toX, toY);
  }
}

function drawArrowHead(
  context: CanvasRenderingContext2D,
  fromX: number,
  fromY: number,
  toX: number,
  toY: number,
) {
  const angle = Math.atan2(toY - fromY, toX - fromX);
  const arrowLength = 14;

  context.beginPath();
  context.moveTo(toX, toY);
  context.lineTo(
    toX - arrowLength * Math.cos(angle - Math.PI / 6),
    toY - arrowLength * Math.sin(angle - Math.PI / 6),
  );
  context.moveTo(toX, toY);
  context.lineTo(
    toX - arrowLength * Math.cos(angle + Math.PI / 6),
    toY - arrowLength * Math.sin(angle + Math.PI / 6),
  );
  context.stroke();
}

function drawCircle(
  context: CanvasRenderingContext2D,
  canvasWidth: number,
  canvasHeight: number,
  drawingData: PlayerAnalysisClipDrawingData,
) {
  const x = getNumber(drawingData.x) * canvasWidth;
  const y = getNumber(drawingData.y) * canvasHeight;
  const radiusX = getNumber(drawingData.radiusX, 0.05) * canvasWidth;
  const radiusY = getNumber(drawingData.radiusY, 0.05) * canvasHeight;

  context.beginPath();
  context.strokeStyle = getString(drawingData.color, DEFAULT_COLOR);
  context.lineWidth = getNumber(drawingData.lineWidth, DEFAULT_LINE_WIDTH);
  context.ellipse(x, y, radiusX, radiusY, 0, 0, Math.PI * 2);
  context.stroke();
}

function drawBox(
  context: CanvasRenderingContext2D,
  canvasWidth: number,
  canvasHeight: number,
  drawingData: PlayerAnalysisClipDrawingData,
  drawingType: PlayerAnalysisClipDrawingType,
) {
  const x = getNumber(drawingData.x) * canvasWidth;
  const y = getNumber(drawingData.y) * canvasHeight;
  const width = getNumber(drawingData.width) * canvasWidth;
  const height = getNumber(drawingData.height) * canvasHeight;

  context.beginPath();
  context.strokeStyle = getString(drawingData.color, DEFAULT_COLOR);
  context.lineWidth = getNumber(drawingData.lineWidth, DEFAULT_LINE_WIDTH);
  context.strokeRect(x, y, width, height);

  if (drawingType === "AREA") {
    context.globalAlpha = 0.15;
    context.fillStyle = getString(drawingData.color, DEFAULT_COLOR);
    context.fillRect(x, y, width, height);
    context.globalAlpha = 1;
  }
}

function drawText(
  context: CanvasRenderingContext2D,
  canvasWidth: number,
  canvasHeight: number,
  drawingData: PlayerAnalysisClipDrawingData,
) {
  const x = getNumber(drawingData.x) * canvasWidth;
  const y = getNumber(drawingData.y) * canvasHeight;
  const text = getString(drawingData.text, "");
  const fontSize = getNumber(drawingData.fontSize, DEFAULT_FONT_SIZE);

  if (!text) {
    return;
  }

  context.fillStyle = getString(drawingData.color, DEFAULT_COLOR);
  context.font = `${fontSize}px sans-serif`;
  context.textBaseline = "top";
  context.fillText(text, x, y);
}

function getNumber(value: unknown, defaultValue = 0): number {
  return typeof value === "number" && Number.isFinite(value)
    ? value
    : defaultValue;
}

function getString(value: unknown, defaultValue: string): string {
  return typeof value === "string" ? value : defaultValue;
}
