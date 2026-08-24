// Math Keypad - mirrors MathKeypad.kt

interface MathKeypadProps {
  onDigit: (d: string) => void;
  onBackspace: () => void;
  onClear: () => void;
  onSubmit: () => void;
  showMultiplySymbol?: boolean;
  showMinusSymbol?: boolean;
  showDecimal?: boolean;
}

export default function MathKeypad({
  onDigit,
  onBackspace,
  onClear,
  onSubmit,
  showMultiplySymbol = false,
  showMinusSymbol = false,
  showDecimal = false,
}: MathKeypadProps) {
  return (
    <div className="w-full rounded-t-3xl bg-slate-100 px-3 py-3">
      {/* Row 1 */}
      <div className="flex gap-2 mb-2">
        <KeypadButton label="1" onPress={() => onDigit("1")} />
        <KeypadButton label="2" onPress={() => onDigit("2")} />
        <KeypadButton label="3" onPress={() => onDigit("3")} />
        <KeypadIconButton label="⌫" className="bg-slate-200 text-slate-700" onPress={onBackspace} />
      </div>

      {/* Row 2 */}
      <div className="flex gap-2 mb-2">
        <KeypadButton label="4" onPress={() => onDigit("4")} />
        <KeypadButton label="5" onPress={() => onDigit("5")} />
        <KeypadButton label="6" onPress={() => onDigit("6")} />
        {showMultiplySymbol ? (
          <KeypadButton label="×" className="bg-indigo-100 text-primary-indigo" onPress={() => onDigit("*")} />
        ) : showMinusSymbol ? (
          <KeypadButton label="-" className="bg-rose-100 text-rose-700" onPress={() => onDigit("-")} />
        ) : (
          <KeypadButton label="C" className="bg-slate-200 text-slate-700" onPress={onClear} />
        )}
      </div>

      {/* Row 3 */}
      <div className="flex gap-2 mb-2">
        <KeypadButton label="7" onPress={() => onDigit("7")} />
        <KeypadButton label="8" onPress={() => onDigit("8")} />
        <KeypadButton label="9" onPress={() => onDigit("9")} />
        {showDecimal ? (
          <KeypadButton label="." onPress={() => onDigit(".")} />
        ) : showMultiplySymbol && showMinusSymbol ? (
          <KeypadButton label="-" className="bg-rose-100 text-rose-700" onPress={() => onDigit("-")} />
        ) : (
          <KeypadButton label="C" className="bg-slate-200 text-slate-700" onPress={onClear} />
        )}
      </div>

      {/* Row 4 */}
      <div className="flex gap-2">
        <KeypadButton label="0" onPress={() => onDigit("0")} />
        {showMultiplySymbol && !showDecimal && (
          <KeypadButton label="C" className="bg-slate-200 text-slate-700" onPress={onClear} />
        )}
        <button
          onClick={onSubmit}
          className={`h-13 py-3 rounded-xl bg-primary-indigo text-white font-black tracking-wider flex items-center justify-center gap-1.5 transition-transform active:scale-95 ${
            showMultiplySymbol && !showDecimal ? "flex-[2]" : "flex-[3]"
          }`}
        >
          SUBMIT ✓
        </button>
      </div>
    </div>
  );
}

function KeypadButton({
  label,
  className = "bg-white text-slate-800",
  onPress,
}: {
  label: string;
  className?: string;
  onPress: () => void;
}) {
  return (
    <button
      onClick={onPress}
      className={`flex-1 h-13 py-3 rounded-xl text-xl font-bold transition-transform active:scale-95 ${className}`}
    >
      {label}
    </button>
  );
}

function KeypadIconButton({
  label,
  className = "bg-slate-200 text-slate-700",
  onPress,
}: {
  label: string;
  className?: string;
  onPress: () => void;
}) {
  return (
    <button
      onClick={onPress}
      className={`flex-1 h-13 py-3 rounded-xl text-xl font-bold transition-transform active:scale-95 ${className}`}
    >
      {label}
    </button>
  );
}