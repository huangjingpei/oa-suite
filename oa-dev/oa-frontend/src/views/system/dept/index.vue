<template>
  <div class="system-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <ApartmentOutlined style="color: #1677ff; margin-right: 8px" />
          <span>组织架构与部门管理</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" @click="openCreateModal(0)">
              <template #icon><PlusOutlined /></template>
              新增顶级部门
            </a-button>
            <a-button @click="loadData">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="deptTree"
        :loading="loading"
        row-key="id"
        style="margin-top: 16px"
        :pagination="false"
        :default-expand-all-rows="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deptName'">
            <span style="font-weight: 500">{{ record.deptName }}</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 1" color="success">正常启用</a-tag>
            <a-tag v-else color="default">停用</a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openCreateModal(record.id)">
                + 子部门
              </a-button>
              <a-button type="link" size="small" @click="openEditModal(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该组织部门吗？"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 部门编辑/新增模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalMode === 'create' ? '新增组织部门' : '编辑组织部门'"
      width="540px"
      :confirm-loading="submitting"
      @ok="handleModalSubmit"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="上级部门">
          <a-tree-select
            v-model:value="formData.parentId"
            :tree-data="parentTreeOptions"
            placeholder="选择父级部门（留空则为顶级部门）"
            allow-clear
            tree-default-expand-all
            style="width: 100%"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="14">
            <a-form-item label="部门全称" required>
              <a-input v-model:value="formData.deptName" placeholder="例如：技术研发中心" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item label="部门编码">
              <a-input v-model:value="formData.deptCode" placeholder="如 DEPT_DEV" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="部门负责人">
              <a-input v-model:value="formData.leader" placeholder="负责人姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系电话">
              <a-input v-model:value="formData.phone" placeholder="办公电话或手机" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="显示顺序">
              <a-input-number v-model:value="formData.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="启用状态">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">正常</a-radio>
                <a-radio :value="0">停用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  ApartmentOutlined,
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import {
  getDeptTreeApi,
  createDeptApi,
  updateDeptApi,
  deleteDeptApi,
  type DeptItem
} from '@/api/system';

const loading = ref(false);
const submitting = ref(false);
const rawDeptList = ref<DeptItem[]>([]);
const deptTree = ref<DeptItem[]>([]);

const modalVisible = ref(false);
const modalMode = ref<'create' | 'edit'>('create');

const formData = reactive<DeptItem>({
  id: undefined,
  parentId: 0,
  deptCode: '',
  deptName: '',
  leader: '',
  phone: '',
  email: '',
  sortOrder: 1,
  status: 1
});

const columns = [
  { title: '组织机构名称', dataIndex: 'deptName', key: 'deptName' },
  { title: '部门编码', dataIndex: 'deptCode', key: 'deptCode', width: 140 },
  { title: '负责人', dataIndex: 'leader', key: 'leader', width: 120 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 180, align: 'center' as const }
];

const buildTree = (items: DeptItem[], parentId: number = 0): DeptItem[] => {
  const result: DeptItem[] = [];
  for (const item of items) {
    if (Number(item.parentId || 0) === parentId) {
      const children = buildTree(items, Number(item.id));
      if (children.length) item.children = children;
      result.push(item);
    }
  }
  return result;
};

const parentTreeOptions = computed(() => {
  const format = (items: DeptItem[]): any[] => {
    return items.map(item => ({
      title: item.deptName,
      value: item.id,
      children: item.children ? format(item.children) : []
    }));
  };
  return [
    { title: '顶级根部门 (无上级)', value: 0 },
    ...format(deptTree.value)
  ];
});

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getDeptTreeApi();
    if (res.code === 200 && res.data) {
      rawDeptList.value = res.data;
      deptTree.value = buildTree(JSON.parse(JSON.stringify(res.data)), 0);
    }
  } catch {
    // 演示回退
    const demo: DeptItem[] = [
      { id: 1001, parentId: 0, deptCode: 'CORP', deptName: '启航协同集团总部', leader: '王董事长', phone: '010-88880001', sortOrder: 1, status: 1 },
      { id: 1002, parentId: 1001, deptCode: 'DEV', deptName: '技术研发中心', leader: '李总工', phone: '010-88880002', sortOrder: 1, status: 1 },
      { id: 1003, parentId: 1001, deptCode: 'HR', deptName: '人力行政中心', leader: '张总监', phone: '010-88880003', sortOrder: 2, status: 1 },
      { id: 1004, parentId: 1001, deptCode: 'MKT', deptName: '市场营销中心', leader: '赵总监', phone: '010-88880004', sortOrder: 3, status: 1 }
    ];
    rawDeptList.value = demo;
    deptTree.value = buildTree(demo, 0);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = (parentId: number = 0) => {
  modalMode.value = 'create';
  formData.id = undefined;
  formData.parentId = parentId;
  formData.deptCode = '';
  formData.deptName = '';
  formData.leader = '';
  formData.phone = '';
  formData.email = '';
  formData.sortOrder = 1;
  formData.status = 1;
  modalVisible.value = true;
};

const openEditModal = (record: DeptItem) => {
  modalMode.value = 'edit';
  formData.id = record.id;
  formData.parentId = record.parentId || 0;
  formData.deptCode = record.deptCode;
  formData.deptName = record.deptName;
  formData.leader = record.leader || '';
  formData.phone = record.phone || '';
  formData.email = record.email || '';
  formData.sortOrder = record.sortOrder || 1;
  formData.status = record.status ?? 1;
  modalVisible.value = true;
};

const handleModalSubmit = async () => {
  if (!formData.deptName.trim()) {
    message.warning('请输入部门全称');
    return;
  }
  try {
    submitting.value = true;
    if (modalMode.value === 'create') {
      const res = await createDeptApi(formData);
      if (res.code === 200) {
        message.success('新增部门成功');
        modalVisible.value = false;
        loadData();
      }
    } else {
      const res = await updateDeptApi(formData);
      if (res.code === 200) {
        message.success('修改部门成功');
        modalVisible.value = false;
        loadData();
      }
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (id?: number) => {
  if (!id) return;
  try {
    const res = await deleteDeptApi(id);
    if (res.code === 200) {
      message.success('部门删除成功');
      loadData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

onMounted(() => {
  loadData();
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
</style>
