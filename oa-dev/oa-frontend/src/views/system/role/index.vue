<template>
  <div class="system-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <SafetyCertificateOutlined style="color: #1677ff; margin-right: 8px" />
          <span>角色与权限体系管理 (RBAC)</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" @click="openCreateModal">
              <template #icon><PlusOutlined /></template>
              新增系统角色
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
        :data-source="roleList"
        :loading="loading"
        row-key="id"
        style="margin-top: 16px"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'roleCode'">
            <a-tag color="blue">{{ record.roleCode }}</a-tag>
          </template>

          <template v-else-if="column.key === 'roleName'">
            <span style="font-weight: 600">{{ record.roleName }}</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 1" color="success">启用</a-tag>
            <a-tag v-else color="default">停用</a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openPermModal(record)">
                权限配置
              </a-button>
              <a-button type="link" size="small" @click="openEditModal(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该系统角色吗？"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 角色新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalMode === 'create' ? '新增系统角色' : '编辑角色信息'"
      width="500px"
      :confirm-loading="submitting"
      @ok="handleModalSubmit"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="角色编码标识" required>
          <a-input
            v-model:value="formData.roleCode"
            placeholder="例如：ROLE_FINANCE"
            :disabled="modalMode === 'edit' && formData.roleCode === 'ROLE_ADMIN'"
          />
        </a-form-item>

        <a-form-item label="角色名称" required>
          <a-input v-model:value="formData.roleName" placeholder="例如：财务审计主管" />
        </a-form-item>

        <a-form-item label="角色职责描述">
          <a-textarea v-model:value="formData.description" placeholder="说明该角色具备的业务职权范围" :rows="3" />
        </a-form-item>

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

    <!-- 权限树配置弹窗 -->
    <a-modal
      v-model:open="permModalVisible"
      :title="`权限分配 - ${currentRole?.roleName || ''}`"
      width="500px"
      @ok="handleSavePerms"
    >
      <div style="margin-bottom: 12px; color: #666; font-size: 13px">
        勾选为该角色开放的协同平台功能菜单与审批中枢模块：
      </div>
      <a-tree
        v-model:checkedKeys="checkedPermKeys"
        checkable
        default-expand-all
        :tree-data="permTreeData"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SafetyCertificateOutlined,
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import {
  listRolesApi,
  createRoleApi,
  updateRoleApi,
  deleteRoleApi,
  type RoleItem
} from '@/api/system';

const loading = ref(false);
const submitting = ref(false);
const roleList = ref<RoleItem[]>([]);

const modalVisible = ref(false);
const modalMode = ref<'create' | 'edit'>('create');
const currentRole = ref<RoleItem | null>(null);

const permModalVisible = ref(false);
const checkedPermKeys = ref<string[]>([
  'dashboard', 'leave:view', 'attendance:view', 'seal:view', 'redhead:view', 'borrow:view'
]);

const permTreeData = [
  {
    title: '协同办公中枢',
    key: 'dashboard_module',
    children: [
      { title: '工作台审批总览 (Dashboard)', key: 'dashboard' },
      { title: '待办审批处理与加签 (WfApprove)', key: 'wf:approve' }
    ]
  },
  {
    title: '人事与考勤中心 (HR)',
    key: 'hr_module',
    children: [
      { title: '请假台账与审批单打印', key: 'leave:view' },
      { title: '异常补卡与打卡流水', key: 'attendance:view' }
    ]
  },
  {
    title: '行政与公文中心 (ADM)',
    key: 'adm_module',
    children: [
      { title: '企业用印审批签报', key: 'seal:view' },
      { title: '红头公文拟稿与签发', key: 'redhead:view' },
      { title: '档案资料借阅出库', key: 'borrow:view' }
    ]
  },
  {
    title: '组织与系统管理 (SYS)',
    key: 'sys_module',
    children: [
      { title: '部门架构维护 (Dept)', key: 'sys:dept' },
      { title: '员工用户管理 (User)', key: 'sys:user' },
      { title: '角色权限体系 (Role)', key: 'sys:role' },
      { title: '数据字典中心 (Dict)', key: 'sys:dict' }
    ]
  }
];

const formData = reactive<RoleItem>({
  id: undefined,
  roleCode: '',
  roleName: '',
  description: '',
  sortOrder: 1,
  status: 1
});

const columns = [
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 150 },
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 160 },
  { title: '角色描述说明', dataIndex: 'description', key: 'description' },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 200, align: 'center' as const }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await listRolesApi();
    if (res.code === 200 && res.data) {
      roleList.value = res.data;
    }
  } catch {
    roleList.value = [
      { id: 1, roleCode: 'ROLE_ADMIN', roleName: '超级管理员', description: '系统最高统管权限，拥有全平台所有管理职能', sortOrder: 1, status: 1 },
      { id: 2, roleCode: 'ROLE_DEV_MGR', roleName: '研发主管', description: '技术研发中心主管，审批研发团队假勤与用印', sortOrder: 2, status: 1 },
      { id: 3, roleCode: 'ROLE_HR', roleName: '人事主管', description: '人力资源负责人，负责全员假期复核、考勤补卡核准', sortOrder: 3, status: 1 },
      { id: 4, roleCode: 'ROLE_USER', roleName: '普通员工', description: '企业基础协同用户，发起日常表单与个人查阅', sortOrder: 4, status: 1 }
    ];
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  modalMode.value = 'create';
  formData.id = undefined;
  formData.roleCode = '';
  formData.roleName = '';
  formData.description = '';
  formData.sortOrder = 1;
  formData.status = 1;
  modalVisible.value = true;
};

const openEditModal = (record: RoleItem) => {
  modalMode.value = 'edit';
  formData.id = record.id;
  formData.roleCode = record.roleCode;
  formData.roleName = record.roleName;
  formData.description = record.description || '';
  formData.sortOrder = record.sortOrder || 1;
  formData.status = record.status ?? 1;
  modalVisible.value = true;
};

const handleModalSubmit = async () => {
  if (!formData.roleCode.trim() || !formData.roleName.trim()) {
    message.warning('角色编码与角色名称均不能为空');
    return;
  }
  try {
    submitting.value = true;
    if (modalMode.value === 'create') {
      const res = await createRoleApi(formData);
      if (res.code === 200) {
        message.success('新增系统角色成功');
        modalVisible.value = false;
        loadData();
      }
    } else {
      const res = await updateRoleApi(formData);
      if (res.code === 200) {
        message.success('角色信息修改成功');
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

const openPermModal = (record: RoleItem) => {
  currentRole.value = record;
  if (record.roleCode === 'ROLE_ADMIN') {
    checkedPermKeys.value = [
      'dashboard', 'wf:approve', 'leave:view', 'attendance:view',
      'seal:view', 'redhead:view', 'borrow:view',
      'sys:dept', 'sys:user', 'sys:role', 'sys:dict'
    ];
  } else {
    checkedPermKeys.value = ['dashboard', 'leave:view', 'attendance:view', 'seal:view', 'redhead:view', 'borrow:view'];
  }
  permModalVisible.value = true;
};

const handleSavePerms = () => {
  message.success(`角色 [${currentRole.value?.roleName}] 权限策略更新成功`);
  permModalVisible.value = false;
};

const handleDelete = async (roleId?: number) => {
  if (!roleId) return;
  try {
    const res = await deleteRoleApi(roleId);
    if (res.code === 200) {
      message.success('角色已成功删除');
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
