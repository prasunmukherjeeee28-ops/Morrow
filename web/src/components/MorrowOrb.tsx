import { Canvas } from '@react-three/fiber';
import { Float, MeshDistortMaterial, Sphere } from '@react-three/drei';

function OrbCore() {
  return (
    <>
      <ambientLight intensity={0.72} />
      <pointLight position={[3, 2, 4]} intensity={5.5} color="#d7c8ff" />
      <pointLight position={[-3, -2, -3]} intensity={3.2} color="#6650d8" />
      <Float speed={0.75} rotationIntensity={0.16} floatIntensity={0.34}>
        <Sphere args={[1.08, 48, 48]}>
          <MeshDistortMaterial
            color="#8d79d8"
            roughness={0.34}
            metalness={0.48}
            distort={0.2}
            speed={1.05}
          />
        </Sphere>
      </Float>
    </>
  );
}

export default function MorrowOrb() {
  return (
    <div className="orb-scene" role="img" aria-label="Morrow's animated violet core">
      <div className="orb-fallback" />
      <Canvas
        camera={{ position: [0, 0, 3.65], fov: 42 }}
        dpr={1}
        gl={{ alpha: true, antialias: false, powerPreference: 'low-power' }}
      >
        <OrbCore />
      </Canvas>
      <div className="orb-halo" />
      <div className="orb-ring orb-ring-one" />
      <div className="orb-ring orb-ring-two" />
    </div>
  );
}
