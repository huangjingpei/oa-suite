<template>
  <div class="system-page">
    <a-row :gutter="16">
      <!-- 左侧字典分类 -->
      <a-col :span="8">
        <a-card :bordered="false" class="page-card" style="height: 100%">
          <div class="table-header">
            <div class="header-title">
              <BookOutlined style="color: #1677ff; margin-right: 6px" />
              <span>数据字典类型</span>
            </div>
            <a-button type="primary" size="small" @click="openCreateTypeModal">
              + 新增分类
            </a-button>
          </div>

          <a-list
            :data-source="typeList"
            :loading="typeLoading"
            style="margin-top: 12px"
            item-layout="horizontal"
          >
            <template #renderItem="{ item }">
              <a-list-item
                class="dict-type-item"
                :class="{ active: currentType?.id === item.id }"
                @click="selectType(item)"
              >
                <a-list-item-meta :description="item.dictType">
                  <template #title>
                    <span style="font-weight: 500">{{ item.dictName }}</span>
                  </template>
                </a-list-item-meta>
                <div @click.stop>
                  <a-space>
                    <a-button type="link" size="small" @click="openEditTypeModal(item)">编辑</a-button>
                    <a-popconfirm
                      title="确定删除该字典类型吗？"
                      @confirm="handleDeleteType(item.id)"
                    >
                      <a-button type="link" danger size="small">删除</a-button>
                    </a-popconfirm>
                  </a-space>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 右侧字典键值明细列表 -->
      <a-col :span="16">
        <a-card :bordered="false" class="page-card">
          <div class="table-header">
            <div>
              <span style="font-weight: 600; font-size: 15px">字典项列表</span>
              <a-tag v-if="currentType" color="blue" style="margin-left: 8px">
                {{ currentType.dictName }} ({{ currentType.dictType }})
              </a-tag>
            </div>
            <a-space>
              <a-button type="primary" :disabled="!currentType" @click="openCreateDataModal">
                <template #icon><PlusOutlined /></template>
                新增字典项
              </a-button>
              <a-button @click="loadDictData">
                <template #icon><ReloadOutlined /></template>
                刷新
              </a-button>
            </a-space>
          </div>

          <a-table
            :columns="dataColumns"
            :data-source="dataList"
            :loading="dataLoading"
            row-key="id"
            style="margin-top: 16px"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'dictLabel'">
                <strong>{{ record.dictLabel }}</strong>
              </template>

              <template v-else-if="column.key === 'dictValue'">
                <a-tag color="purple">{{ record.dictValue }}</a-tag>
              </template>

              <template v-else-if="column.key === 'status'">
                <a-tag v-if="record.status === 1" color="success">正常</a-tag>
                <a-tag v-else color="default">停用</a-tag>
              </template>

              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="openEditDataModal(record)">编辑</a-button>
                  <a-popconfirm
                    title="确定删除该字典项吗？"
                    @confirm="handleDeleteData(record.id)"
                  >
                    <a-button type="link" danger size="small">删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 字典分类新增/修改弹窗 -->
    <a-modal
      v-model:open="typeModalVisible"
      :title="typeModalMode === 'create' ? '新增字典分类' : '编辑字典分类'"
      width="480px"
      @ok="handleTypeSubmit"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="字典类型编码" required>
          <a-input v-model:value="typeFormData.dictType" placeholder="例如：oa_custom_status" />
        </a-form-item>
        <a-form-item label="字典名称" required>
          <a-input v-model:value="typeFormData.dictName" placeholder="例如：自定义业务状态" />
        </a-form-item>
        <a-form-item label="备注说明">
          <a-input v-model:value="typeFormData.remark" placeholder="备注用途" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 字典项新增/修改弹窗 -->
    <a-modal
      v-model:open="dataModalVisible"
      :title="dataModalMode === 'create' ? '新增字典数据项' : '编辑字典数据项'"
      width="480px"
      @ok="handleDataSubmit"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="所属字典类型">
          <a-input :value="currentType?.dictName" disabled />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="字典显示标签" required>
              <a-input v-model:value="dataFormData.dictLabel" placeholder="如：审核通过" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="字典实际键值" required>
              <a-input v-model:value="dataFormData.dictValue" placeholder="如：PASS / 2" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排序权重">
              <a-input-number v-model:value="dataFormData.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-radio-group v-model:value="dataFormData.status">
                <a-radio :value="1">正常</a-radio>
                <a-radio :value="0">停用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注说明">
          <a-input v-model:value="dataFormData.remark" placeholder="备注说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  BookOutlined,
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import {
  listDictTypesApi,
  createDictTypeApi,
  updateDictTypeApi,
  deleteDictTypeApi,
  listDictDataByTypeApi,
  createDictDataApi,
  updateDictDataApi,
  deleteDictDataApi,
  type DictTypeItem,
  type DictDataItem
} from '@/api/system';

const typeLoading = ref(false);
const dataLoading = ref(false);

const typeList = ref<DictTypeItem[]>([]);
const currentType = ref<DictTypeItem | null>(null);
const dataList = ref<DictDataItem[]>([]);

// 弹窗状态
const typeModalVisible = ref(false);
const typeModalMode = ref<'create' | 'edit'>('create');
const typeFormData = reactive<DictTypeItem>({
  id: undefined,
  dictName: '',
  dictType: '',
  remark: ''
});

const dataModalVisible = ref(false);
const dataModalMode = ref<'create' | 'edit'>('create');
const dataFormData = reactive<DictDataItem>({
  id: undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sortOrder: 1,
  status: 1,
  remark: ''
});

const dataColumns = [
  { title: '字典显示标签', dataIndex: 'dictLabel', key: 'dictLabel', width: 160 },
  { title: '字典数据键值', dataIndex: 'dictValue', key: 'dictValue', width: 140 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 140, align: 'center' as const }
];

const loadTypes = async () => {
  typeLoading.value = true;
  try {
    const res = await listDictTypesApi();
    if (res.code === 200 && res.data) {
      typeList.value = res.data;
      if (res.data.length && !currentType.value) {
        selectType(res.data[0]);
      }
    }
  } catch {
    typeList.value = [
      { id: 5001, dictName: '请假假期类型', dictType: 'oa_leave_type' },
      { id: 5002, dictName: '印章使用类别', dictType: 'oa_seal_type' },
      { id: 5003, dictName: '红头公文类型', dictType: 'oa_redhead_type' },
      { id: 5004, dictName: '流程审批状态', dictType: 'oa_proc_status' }
    ];
    if (typeList.value.length && !currentType.value) {
      selectType(typeList.value[0]);
    }
  } finally {
    typeLoading.value = false;
  }
};

const selectType = (item: DictTypeItem) => {
  currentType.value = item;
  loadDictData();
};

const loadDictData = async () => {
  if (!currentType.value) return;
  dataLoading.value = true;
  try {
    const res = await listDictDataByTypeApi(currentType.value.dictType);
    if (res.code === 200 && res.data) {
      dataList.value = res.data;
    }
  } catch {
    if (currentType.value.dictType === 'oa_leave_type') {
      dataList.value = [
        { id: 5101, dictType: 'oa_leave_type', dictLabel: '年休假', dictValue: 'ANNUAL', sortOrder: 1, status: 1 },
        { id: 5102, dictType: 'oa_leave_type', dictLabel: '事假', dictValue: 'PERSONAL', sortOrder: 2, status: 1 },
        { id: 5103, dictType: 'oa_leave_type', dictLabel: '病假', dictValue: 'SICK', sortOrder: 3, status: 1 },
        { id: 5104, dictType: 'oa_leave_type', dictLabel: '调休假', dictValue: 'REST', sortOrder: 4, status: 1 }
      ];
    } else {
      dataList.value = [
        { id: 5201, dictType: 'oa_seal_type', dictLabel: '企业公章', dictValue: 'COMMON', sortOrder: 1, status: 1 },
        { id: 5202, dictType: 'oa_seal_type', dictLabel: '合同专用章', dictValue: 'CONTRACT', sortOrder: 2, status: 1 }
      ];
    }
  } finally {
    dataLoading.value = false;
  }
};

const openCreateTypeModal = () => {
  typeModalMode.value = 'create';
  typeFormData.id = undefined;
  typeFormData.dictName = '';
  typeFormData.dictType = '';
  typeFormData.remark = '';
  typeModalVisible.value = true;
};

const openEditTypeModal = (item: DictTypeItem) => {
  typeModalMode.value = 'edit';
  typeFormData.id = item.id;
  typeFormData.dictName = item.dictName;
  typeFormData.dictType = item.dictType;
  typeFormData.remark = item.remark || '';
  typeModalVisible.value = true;
};

const handleTypeSubmit = async () => {
  if (!typeFormData.dictName.trim() || !typeFormData.dictType.trim()) {
    message.warning('请填写字典类型编码与字典名称');
    return;
  }
  try {
    if (typeModalMode.value === 'create') {
      const res = await createDictTypeApi(typeFormData);
      if (res.code === 200) {
        message.success('新增字典分类成功');
        typeModalVisible.value = false;
        loadTypes();
      }
    } else {
      const res = await updateDictTypeApi(typeFormData);
      if (res.code === 200) {
        message.success('修改字典分类成功');
        typeModalVisible.value = false;
        loadTypes();
      }
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

const handleDeleteType = async (id?: number) => {
  if (!id) return;
  try {
    const res = await deleteDictTypeApi(id);
    if (res.code === 200) {
      message.success('字典分类删除成功');
      loadTypes();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

const openCreateDataModal = () => {
  if (!currentType.value) return;
  dataModalMode.value = 'create';
  dataFormData.id = undefined;
  dataFormData.dictType = currentType.value.dictType;
  dataFormData.dictLabel = '';
  dataFormData.dictValue = '';
  dataFormData.sortOrder = (dataList.value.length + 1);
  dataFormData.status = 1;
  dataFormData.remark = '';
  dataModalVisible.value = true;
};

const openEditDataModal = (item: DictDataItem) => {
  dataModalMode.value = 'edit';
  dataFormData.id = item.id;
  dataFormData.dictType = item.dictType;
  dataFormData.dictLabel = item.dictLabel;
  dataFormData.dictValue = item.dictValue;
  dataFormData.sortOrder = item.sortOrder || 1;
  dataFormData.status = item.status ?? 1;
  dataFormData.remark = item.remark || '';
  dataModalVisible.value = true;
};

const handleDataSubmit = async () => {
  if (!dataFormData.dictLabel.trim() || !dataFormData.dictValue.trim()) {
    message.warning('请填写字典标签与键值');
    return;
  }
  try {
    if (dataModalMode.value === 'create') {
      const res = await createDictDataApi(dataFormData);
      if (res.code === 200) {
        message.success('新增字典项成功');
        dataModalVisible.value = false;
        loadDictData();
      }
    } else {
      const res = await updateDictDataApi(dataFormData);
      if (res.code === 200) {
        message.success('修改字典项成功');
        dataModalVisible.value = false;
        loadDictData();
      }
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

const handleDeleteData = async (id?: number) => {
  if (!id) return;
  try {
    const res = await deleteDictDataApi(id);
    if (res.code === 200) {
      message.success('字典项已删除');
      loadDictData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

onMounted(() => {
  loadTypes();
});
</script>

<style scoped>
.system-page {
  padding: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  display: flex;
  align-items: center;
}

.dict-type-item {
  cursor: pointer;
  padding: 10px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}

.dict-type-item:hover {
  background-color: #f0f7ff;
}

.dict-type-item.active {
  background-color: #e6f4ff;
  border-left: 3px solid #1677ff;
}
</style>
