#!/bin/bash

echo "🛑 Stopping RemVault Kubernetes Cluster..."

# Delete Deployments and Services, but LEAVE the PersistentVolumeClaims alone!
kubectl delete deployments --all
kubectl delete services --all

echo "======================================================="
echo "✅ Cluster applications and network routes deleted."
echo "💾 Database storage (postgres-pvc) is safe and preserved."
echo "======================================================="