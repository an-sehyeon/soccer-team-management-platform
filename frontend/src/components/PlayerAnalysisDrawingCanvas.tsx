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
}: PlayerAnalysisDrawingCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [dragStartPoint, setDragStartPoint] = useState<Point | null>(null);

  function resizeCanvas() {
    const canvas = canvasRef.current;
    const parent = canvas?.parentElement;

    if (!canvas || !parent) {
      return;
    }

    const rect = parent.getBoundingClientRect();

    if (rect.width <= 0 || rect.height <= 0) {
      return;
    }

    const nextWidth = Math.round(rect.width);
    const nextHeight = Math.round(rect.height);

    if (canvas.width !== nextWidth) {
      canvas.width = nextWidth;
    }

    if (canvas.height !== nextHeight) {
      canvas.height = nextHeight;
    }
  }

  function getMousePoint(event: MouseEvent<HTMLCanvasElement>): Point | null {
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
  }

  function handleMouseDown(event: MouseEvent<HTMLCanvasElement>) {
    if (!isDrawingMode) {
      return;
    }

    const point = getMousePoint(event);

    if (!point) {
      return;
    }

    if (drawingType === "TEXT") {
      onDraftDrawingData({
        version: 1,
        x: point.x,
        y: point.y,
        text: drawingText.trim() || "텍스트",
        color: DEFAULT_COLOR,
        fontSize: DEFAULT_FONT_SIZE,
      });
      return;
    }

    setDragStartPoint(point);
  }

  function handleMouseUp(event: MouseEvent<HTMLCanvasElement>) {
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
  }

  useEffect(() => {
    const canvas = canvasRef.current;
    const parent = canvas?.parentElement;

    if (!canvas || !parent) {
      return;
    }

    resizeCanvas();

    const resizeObserver = new ResizeObserver(() => {
      resizeCanvas();
      drawCanvas(canvas, drawings, currentTimeSec);
    });

    resizeObserver.observe(parent);

    return () => {
      resizeObserver.disconnect();
    };
  }, [currentTimeSec, drawings]);

  useEffect(() => {
    const canvas = canvasRef.current;

    if (!canvas) {
      return;
    }

    resizeCanvas();
    drawCanvas(canvas, drawings, currentTimeSec);
  }, [drawings, currentTimeSec]);

  return (
    <canvas
      ref={canvasRef}
      className={
        isDrawingMode
          ? "analysis-drawing-canvas is-drawing"
          : "analysis-drawing-canvas"
      }
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
    />
  );
}

function drawCanvas(
  canvas: HTMLCanvasElement,
  drawings: PlayerAnalysisClipDrawingResponse[],
  currentTimeSec: number,
) {
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
