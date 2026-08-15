import DashboardPrototype, {
  type PrototypeVariant,
} from "./dashboard-prototype";

const variants: PrototypeVariant[] = ["A", "B", "C"];

export default async function DashboardPrototypePage({
  searchParams,
}: {
  searchParams: Promise<{ variant?: string | string[] }>;
}) {
  const value = (await searchParams).variant;
  const requested = Array.isArray(value) ? value[0] : value;
  const initialVariant = variants.includes(requested as PrototypeVariant)
    ? (requested as PrototypeVariant)
    : "A";

  return <DashboardPrototype initialVariant={initialVariant} />;
}
