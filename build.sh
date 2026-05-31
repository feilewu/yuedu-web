#!/bin/bash
set -e

echo "=== yuedu-web 一键构建 ==="

# 1. 构建前端
echo ""
echo ">>> 构建前端..."
cd frontend
pnpm install --ignore-scripts 2>/dev/null
pnpm build-only
mkdir -p ../web/vue
cp -r dist/* ../web/vue/
echo "前端构建完成"

# 2. 构建后端
echo ""
echo ">>> 构建后端..."
cd ..
./gradlew :server:jar --rerun-tasks
echo "后端构建完成"

# 3. 输出
echo ""
echo "=== 构建完成 ==="
echo "产物: server/build/libs/server-1.0.0.jar"
ls -lh server/build/libs/server-1.0.0.jar
