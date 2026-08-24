// SVG line chart with gradient fill - mirrors PerformanceChart.kt

interface PerformanceChartProps {
  times: [number, number][]; // [timestamp, seconds]
  lineColor?: string;
}

export default function PerformanceChart({ times, lineColor = "#4F46E5" }: PerformanceChartProps) {
  if (!times || times.length === 0) {
    return (
      <div className="w-full h-44 rounded-2xl bg-slate-50 flex items-center justify-center">
        <p className="text-slate-400 text-sm font-medium">Complete practice sessions to view your speed chart!</p>
      </div>
    );
  }

  const values = times.map((t) => t[1]);
  const maxVal = Math.max(...values, 1);
  const minVal = Math.min(...values, 0);
  const range = Math.max(maxVal - minVal, 0.5);

  const W = 600;
  const H = 160;
  const padY = 20;
  const chartH = H - padY * 2;

  const points = values.map((v, i) => {
    const x = values.length === 1 ? W / 2 : (i * W) / (values.length - 1);
    const normY = (v - minVal) / range;
    const y = padY + chartH * (1 - normY);
    return { x, y };
  });

  const linePath = points
    .map((p, i) => (i === 0 ? `M ${p.x} ${p.y}` : `L ${p.x} ${p.y}`))
    .join(" ");

  const fillPath = `${linePath} L ${points[points.length - 1].x} ${H} L ${points[0].x} ${H} Z`;

  const fmt = (ts: number) => {
    const d = new Date(ts);
    return `${d.getHours()}:${String(d.getMinutes()).padStart(2, "0")}`;
  };

  return (
    <div className="w-full">
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
          <path d={fillPath} fill="url(#chart-fill)" />
          <path d={linePath} fill="none" stroke={lineColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
          {points.map((p, i) => (
            <g key={i}>
              <circle cx={p.x} cy={p.y} r="7" fill="#FFFFFF" />
              <circle cx={p.x} cy={p.y} r="4.5" fill={lineColor} />
            </g>
          ))}
        </svg>
      </div>
      <div className="flex justify-between px-1 mt-1.5">
        <span className="text-[11px] font-bold text-slate-400">Earlier: {fmt(times[0][0])}</span>
        <span className="text-[11px] font-bold text-slate-400">Recent: {fmt(times[times.length - 1][0])}</span>
      </div>
    </div>
  );
}