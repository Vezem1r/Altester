import { useEffect } from 'react';
import * as THREE from 'three';

const ParticleBackground = () => {
  useEffect(() => {
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(
      75,
      window.innerWidth / window.innerHeight,
      0.1,
      1000
    );
    const renderer = new THREE.WebGLRenderer({ alpha: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.domElement.style.position = 'fixed';
    renderer.domElement.style.top = '0';
    renderer.domElement.style.left = '0';
    renderer.domElement.style.zIndex = '0';
    document.body.appendChild(renderer.domElement);

    const textureLoader = new THREE.TextureLoader();
    const starTexture = textureLoader.load(
      'https://threejs.org/examples/textures/sprites/circle.png'
    );

    const maxStars = 3000;
    const maxStarSize = 1;
    const positions = new Float32Array(maxStars * 3);
    const velocities = new Float32Array(maxStars);
    const sizes = new Float32Array(maxStars);
    let currentStarsCount = 0;

    const createStar = () => {
      if (currentStarsCount < maxStars) {
        const index = currentStarsCount * 3;
        positions[index] = (Math.random() - 0.5) * 1000;
        positions[index + 1] = (Math.random() - 0.5) * 1000;
        positions[index + 2] = Math.random() * 1000;
        velocities[currentStarsCount] = Math.random() < 0.5 ? 0.1 : -0.1;
        sizes[currentStarsCount] = Math.random() * maxStarSize + 1;
        currentStarsCount++;
      }
    };

    for (let i = 0; i < maxStars; i++) {
      createStar();
    }

    const starsGeometry = new THREE.BufferGeometry();
    starsGeometry.setAttribute(
      'position',
      new THREE.BufferAttribute(positions, 3)
    );
    starsGeometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));

    const starsMaterial = new THREE.PointsMaterial({
      color: 0xffffff,
      size: 2,
      sizeAttenuation: true,
      map: starTexture,
      transparent: true,
      depthWrite: false,
    });

    const stars = new THREE.Points(starsGeometry, starsMaterial);
    scene.add(stars);

    camera.position.z = 500;

    const animate = () => {
      requestAnimationFrame(animate);
      const starPositions = starsGeometry.attributes.position.array;
      let visibleStarsCount = 0;

      for (let i = 0; i < currentStarsCount; i++) {
        const index = i * 3;
        starPositions[index] += velocities[i];

        if (starPositions[index] > -500 && starPositions[index] < 500) {
          visibleStarsCount++;
        }

        if (starPositions[index] > 500 || starPositions[index] < -500) {
          starPositions[index] = (Math.random() - 0.5) * 1000;
          starPositions[index + 1] = (Math.random() - 0.5) * 1000;
          starPositions[index + 2] = Math.random() * 1000;
          velocities[i] = Math.random() < 0.5 ? 0.1 : -0.1;
          sizes[i] = Math.random() * maxStarSize;
        }
      }

      starsGeometry.attributes.position.needsUpdate = true;
      starsGeometry.attributes.size.needsUpdate = true;

      while (
        currentStarsCount < maxStars &&
        visibleStarsCount < maxStars - 100
      ) {
        createStar();
      }

      renderer.render(scene, camera);
    };

    animate();

    const onResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    };
    window.addEventListener('resize', onResize);

    return () => {
      window.removeEventListener('resize', onResize);
      document.body.removeChild(renderer.domElement);
    };
  }, []);

  return null;
};

export default ParticleBackground;
