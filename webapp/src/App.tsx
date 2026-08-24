import { useApp } from "./store";
import HomeScreen from "./screens/HomeScreen";
import ConfigScreen from "./screens/ConfigScreen";
import PracticeScreen from "./screens/PracticeScreen";
import GridScreen from "./screens/GridScreen";
import ResultScreen from "./screens/ResultScreen";
import DashboardScreen from "./screens/DashboardScreen";
import PerformanceAnalysisScreen from "./screens/PerformanceAnalysisScreen";
import LearnTablesScreen from "./screens/LearnTablesScreen";
import LearnFactorsScreen from "./screens/LearnFactorsScreen";
import LearnExponentsScreen from "./screens/LearnExponentsScreen";
import LearnRootsScreen from "./screens/LearnRootsScreen";
import PrivacyPolicyScreen from "./screens/PrivacyPolicyScreen";
import UserNameDialog from "./components/UserNameDialog";

export default function App() {
  const { state } = useApp();

  const screen = () => {
    switch (state.destination.type) {
      case "home": return <HomeScreen />;
      case "config": return <ConfigScreen />;
      case "practice": return <PracticeScreen />;
      case "grid": return <GridScreen />;
      case "result": return <ResultScreen />;
      case "dashboard":
      case "history": return <DashboardScreen />;
      case "analysis": return <PerformanceAnalysisScreen />;
      case "learnTables": return <LearnTablesScreen />;
      case "learnFactors": return <LearnFactorsScreen />;
      case "learnExponents": return <LearnExponentsScreen />;
      case "learnRoots": return <LearnRootsScreen />;
      case "privacy": return <PrivacyPolicyScreen />;
      default: return <HomeScreen />;
    }
  };

  return (
    <div className="min-h-screen bg-slate-50">
      {screen()}
      {state.showNamePromptDialog && <UserNameDialog />}
    </div>
  );
}