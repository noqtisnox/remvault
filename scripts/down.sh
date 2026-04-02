#!/bin/bash

echo "🛑 Stopping RemVault Kubernetes Cluster..."

# Delete all resources defined in the k8s folder
kubectl delete -f k8s/

echo "======================================================="
echo "✅ Cluster resources deleted."
echo "💾 Database storage (postgres-pvc) is safe and preserved."
echo "======================================================="