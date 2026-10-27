<template>
  <div class="electronic-stamp-wrapper" :style="{ width: `${size}px`, height: `${size}px` }">
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 200 200"
      xmlns="http://www.w3.org/2000/svg"
      class="stamp-svg"
    >
      <defs>
        <!-- 环形圆弧文字路径 -->
        <path
          id="stamp-text-path"
          d="M 25 100 A 75 75 0 1 1 175 100"
          fill="none"
        />
      </defs>

      <!-- 外双圈 -->
      <circle cx="100" cy="100" r="95" :stroke="color" stroke-width="4" fill="none" opacity="0.88" />
      <circle cx="100" cy="100" r="90" :stroke="color" stroke-width="1.5" fill="none" opacity="0.88" />

      <!-- 环形企业名称文字 -->
      <text
        :fill="color"
        font-size="16"
        font-family="SimSun, 'Songti SC', serif"
        font-weight="bold"
        letter-spacing="2"
      >
        <textPath
          href="#stamp-text-path"
          startOffset="50%"
          text-anchor="middle"
        >
          {{ companyName || '盛泰智慧协同科技有限公司' }}
        </textPath>
      </text>

      <!-- 中心五角星 -->
      <polygon
        points="100,56 109,84 138,84 114,101 123,129 100,112 77,129 86,101 62,84 91,84"
        :fill="color"
        opacity="0.9"
      />

      <!-- 横向印章类型与审批日期 -->
      <text
        x="100"
        y="142"
        text-anchor="middle"
        :fill="color"
        font-size="14"
        font-family="SimHei, 'Heiti SC', sans-serif"
        font-weight="bold"
        letter-spacing="1"
      >
        {{ stampText || '审批专用章' }}
      </text>

      <text
        v-if="date"
        x="100"
        y="162"
        text-anchor="middle"
        :fill="color"
        font-size="11"
        font-family="Consolas, monospace"
        letter-spacing="1"
      >
        {{ date }}
      </text>
    </svg>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    companyName?: string;
    stampText?: string;
    date?: string;
    color?: string;
    size?: number;
  }>(),
  {
    companyName: '盛泰智慧协同科技有限公司',
    stampText: '审批专用章',
    color: '#d32f2f',
    size: 140
  }
);
</script>

<style scoped>
.electronic-stamp-wrapper {
  position: relative;
  display: inline-block;
  user-select: none;
  pointer-events: none;
  transform: rotate(-8deg);
  opacity: 0.85;
  filter: contrast(110%);
}

.stamp-svg {
  display: block;
}
</style>
