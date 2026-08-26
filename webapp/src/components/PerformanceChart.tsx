// SVG line chart with gradient fill - mirrors PerformanceChart.kt (PerformanceDataChart)
// Fastest (lowest value) is plotted at the TOP; best & latest points highlighted.

interface PerformanceChartProps {
  times: [number, number][]; // [timestamp, seconds]
  unit?: string;
  lineColor?: string;
}

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function fmt(ts: number): string {
  const d = new Date(ts);
  return `${MONTHS[d.getMonth()]} ${d.getDate()}, ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

export default function PerformanceChart({ times, unit = "s/q", lineColor = "#4F46E5" }: PerformanceChartProps) {
  if (!times || times.length === 0) {
    return (
      <div className="w-full h-44 rounded-2xl bg-slate-50 flex items-center justify-center">
        <p className="text-slate-400 text-sm font-medium">Complete practice sessions to view your speed chart!</p>
      </div>
    );
  }

  const values = times.map((t) => t[1]);
  const maxVal = Math.max(...values, 0.1);
  const minVal = Math.min(...values, 0);
  const range = Math.max(maxVal - minVal, 0.2);
  const bestIdx = values.indexOf(Math.min(...values));
  const latestIdx = values.length - 1;

  const W = 600;
  const H = 160;
  const padY = 24;
  const chartH = H - padY * 2;

  const points = values.map((v, i) => {
    const x = values.length === 1 ? W / 2 : (i * W) / (values.length - 1);
    const normY = (v - minVal) / range;
    // Lower seconds = faster = plotted higher up (min value at top)
    const y = padY + chartH * normY;
    return { x, y };
  });

  const linePath = points.map((p, i) => (i === 0 ? `M ${p.x} ${p.y}` : `L ${p.x} ${p.y}`)).join(" ");
  const fillPath = `${linePath} L ${points[points.length - 1].x} ${H} L ${points[0].x} ${H} Z`;

  const circleColor = (i: number) =>
    i === bestIdx ? "#059669" : i === latestIdx ? "#4F46E5" : `${lineColor}B3`;

  return (
    <div className="w-full">
      {/* Value guides: Fastest / Slowest */}
      <div className="flex justify-between px-1 mb-1">
        <span className="text-[11px] font-bold text-emerald-600">Fastest: {minVal.toFixed(2)} {unit}</span>
        <span className="text-[11px] font-bold text-slate-400">Slowest: {maxVal.toFixed(2)} {unit}</span>
      </div>

      <div className="w-full h-44 bg-white rounded-2xl border border-slate-200 px-2 py-1.5 overflow-hidden">
        <svg viewBox={`0 0 ${W} ${H}`} className="w-full h-full" preserveAspectRatio="none">
          <defs>
            <linearGradient id="chart-fill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={lineColor} stopOpacity="0.15" />
              <stop offset="100%" stopColor={lineColor} stopOpacity="0.01" />
            </linearGradient>
          </defs>
          {[0, 1, 2, 3].map((i) => (
            <line key={i} x1="0" y1={padY + (chartH * i) / 3} x2={W} y2={padY + (chartH * i) / 3} stroke="#F1F5F9" strokeWidth="2" />
          ))}
          {values.length === 1 ? (
            <>
              <circle cx={points[0].x} cy={points[0].y} r="9" fill={lineColor} />
              <circle cx={points[0].x} cy={points[0].y} r="5" fill="#FFFFFF" />
            </>
          ) : (
            <>
              <path d={fillPath} fill="url(#chart-fill)" />
              <path d={linePath} fill="none" stroke={lineColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
              {points.map((p, i) => {
                const r = i === bestIdx || i === latestIdx ? 7 : 5;
                return (
                  <g key={i}>
                    <circle cx={p.x} cy={p.y} r={r + 3} fill="#FFFFFF" />
                    <circle cx={p.x} cy={p.y} r={r} fill={circleColor(i)} />
                  </g>
                );
              })}
            </>
          )}
        </svg>
      </div>

      <div className="flex justify-between px-1 mt-1.5">
        <span className="text-[11px] font-bold text-slate-400">Earlier: {fmt(times[0][0])}</span>
        <span className="text-[11px] font-bold text-slate-400">Recent: {fmt(times[times.length - 1][0])}</span>
      </div>
    </div>
  );
}