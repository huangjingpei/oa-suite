<template>
  <a-modal
    v-model:open="visible"
    :title="null"
    :footer="null"
    width="900px"
    class="print-preview-modal"
    centered
    destroy-on-close
  >
    <!-- 顶部非打印工具栏 -->
    <div class="print-toolbar no-print">
      <div class="toolbar-title">
        <PrinterOutlined style="color: #1677ff; margin-right: 6px" />
        <span>{{ title || 'A4 高保真纸质打印预览' }}</span>
      </div>
      <div class="toolbar-actions">
        <a-space>
          <a-button @click="zoomOut">缩小</a-button>
          <span class="zoom-text">{{ Math.round(scale * 100) }}%</span>
          <a-button @click="zoomIn">放大</a-button>
          <a-button type="primary" @click="handlePrint">
            <template #icon><PrinterOutlined /></template>
            立即打印 / 另存为 PDF
          </a-button>
          <a-button @click="visible = false">关闭</a-button>
        </a-space>
      </div>
    </div>

    <!-- A4 纸质排版承载容器 -->
    <div class="print-viewport">
      <div
        id="print-paper-content"
        class="print-paper-sheet"
        :style="{ transform: `scale(${scale})`, transformOrigin: 'top center' }"
      >
        <slot />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { PrinterOutlined } from '@ant-design/icons-vue';

const props = defineProps<{
  open: boolean;
  title?: string;
}>();

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void;
}>();

const visible = computed({
  get: () => props.open,
  set: (val: boolean) => emit('update:open', val)
});

const scale = ref<number>(1.0);

const zoomIn = () => {
  if (scale.value < 1.3) scale.value = Number((scale.value + 0.1).toFixed(1));
};

const zoomOut = () => {
  if (scale.value > 0.6) scale.value = Number((scale.value - 0.1).toFixed(1));
};

const handlePrint = () => {
  window.print();
};
</script>

<style scoped>
.print-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  margin: -20px -24px 20px -24px;
  border-radius: 8px 8px 0 0;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.zoom-text {
  font-size: 13px;
  color: #666;
  width: 45px;
  display: inline-block;
  text-align: center;
}

.print-viewport {
  background-color: #e6e8eb;
  padding: 24px;
  display: flex;
  justify-content: center;
  overflow-x: auto;
  min-height: 580px;
}

.print-paper-sheet {
  background: #ffffff;
  width: 210mm;
  min-height: 297mm;
  padding: 20mm 18mm;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  box-sizing: border-box;
  color: #000;
  transition: transform 0.2s ease;
}

/* 浏览器原生打印规则 */
@media print {
  /* 隐藏非打印区域元素 */
  body * {
    visibility: hidden;
  }
  .no-print,
  .no-print * {
    display: none !important;
  }

  /* 仅打印纸张内容并铺满标准 A4 页面 */
  #print-paper-content,
  #print-paper-content * {
    visibility: visible;
  }

  #print-paper-content {
    position: absolute;
    left: 0;
    top: 0;
    width: 210mm !important;
    min-height: 297mm !important;
    margin: 0 !important;
    padding: 15mm 15mm !important;
    box-shadow: none !important;
    transform: none !important;
    background: transparent !important;
  }

  @page {
    size: A4 portrait;
    margin: 0;
  }
}
</style>
