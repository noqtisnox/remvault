#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e 

echo "🚀 Starting RemVault Kubernetes Cluster..."

# 1. Start Minikube if it isn't running
if ! minikube status | grep -q "Running"; then
  echo "📦 Starting Minikube..."
  minikube start --driver=docker
else
  echo "✅ Minikube is already running."
fi

# 2. Point Docker to Minikube's internal network
echo "🐳 Configuring Docker environment..."
eval $(minikube docker-env)

# 3. Build the backend images
echo "🔨 Building Ktor Core image..."
docker build -t remvault-core-image:latest .

echo "🧠 Building Python AI image..."
cd remvault-ai && docker build -t remvault-ai-image:latest . && cd ..

# 4. Deploy the infrastructure and apps
echo "📜 Applying Kubernetes manifests..."
kubectl apply -f k8s/

# 5. Safety catch for Redis (just in case the YAML misses it)
if ! kubectl get svc remvault-cache &> /dev/null; then
    echo "🔧 Registering Redis service..."
    kubectl expose deployment remvault-cache --port=6379 --target-port=6379 --name=remvault-cache
fi

# 6. Wait for the core app to come online
echo "⏳ Waiting for the Ktor API to be ready (this might take a moment)..."
kubectl wait --for=condition=ready pod -l app=core --timeout=120s

echo "======================================================="
echo "🎉 Cluster is ONLINE!"
echo "💾 Your Postgres data is safely mounted to your PVC."
echo ""
echo "👉 FINAL STEP: Open a NEW terminal tab and run:"
echo "kubectl port-forward --address 0.0.0.0 service/remvault-core 8080:8080"
echo "======================================================="