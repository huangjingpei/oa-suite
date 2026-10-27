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
                <template #icon><SafetyCertificateOutlined /></template>
                分配权限
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

    <!-- 权限树配置可视化弹窗 -->
    <a-modal
      v-model:open="permModalVisible"
      :title="`角色权限授权分配 - [${currentRole?.roleName || ''}]`"
      width="640px"
      :confirm-loading="savingPerms"
      @ok="handleSavePerms"
    >
      <a-alert
        message="授权说明"
        description="勾选为该角色开放的协同中枢目录、功能菜单及按钮操作权限；保存后关联用户实时生效。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <div class="perm-tree-toolbar">
        <a-space>
          <a-button size="small" @click="handleExpandAll(true)">展开全部</a-button>
          <a-button size="small" @click="handleExpandAll(false)">折叠全部</a-button>
          <a-button size="small" @click="handleSelectAll(true)">全选所有</a-button>
          <a-button size="small" @click="handleSelectAll(false)">清空选择</a-button>
        </a-space>
        <div class="perm-count-badge">
          已选中 <b>{{ currentSelectedCount }}</b> 项权限
        </div>
      </div>

      <a-spin :spinning="loadingPermTree">
        <div class="perm-tree-container">
          <a-tree
            v-if="permTreeData.length > 0"
            v-model:checkedKeys="checkedPermKeys"
            v-model:expandedKeys="expandedPermKeys"
            checkable
            :tree-data="permTreeData"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            @check="onTreeCheck"
          >
            <template #title="{ name, perms, menuType }">
              <span class="perm-tree-node">
                <a-tag v-if="menuType === 1" color="blue" size="small">目录</a-tag>
                <a-tag v-else-if="menuType === 2" color="cyan" size="small">菜单</a-tag>
                <a-tag v-else color="purple" size="small">按钮</a-tag>
                <span class="perm-node-name">{{ name }}</span>
                <span v-if="perms" class="perm-node-code">{{ perms }}</span>
              </span>
            </template>
          </a-tree>
          <a-empty v-else description="暂无可分配的权限数据" />
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
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
  getPermissionTreeApi,
  getRolePermissionIdsApi,
  assignRolePermissionsApi,
  type RoleItem,
  type PermissionTreeItem
} from '@/api/system';

const loading = ref(false);
const submitting = ref(false);
const roleList = ref<RoleItem[]>([]);

const modalVisible = ref(false);
const modalMode = ref<'create' | 'edit'>('create');
const currentRole = ref<RoleItem | null>(null);

// 权限分配弹窗相关状态
const permModalVisible = ref(false);
const loadingPermTree = ref(false);
const savingPerms = ref(false);
const permTreeData = ref<PermissionTreeItem[]>([]);
const checkedPermKeys = ref<number[]>([]);
const expandedPermKeys = ref<number[]>([]);
const halfCheckedPermKeys = ref<number[]>([]);

const formData = reactive<RoleItem>({
  id: undefined,
  roleCode: '',
  roleName: '',
  description: '',
  sortOrder: 1,
  status: 1
});

const columns = [
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 160 },
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
  { title: '角色职责描述', dataIndex: 'description', key: 'description' },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 220, align: 'center' as const }
];

// 计算选中的权限总数
const currentSelectedCount = computed(() => {
  return checkedPermKeys.value.length;
});

// 提取所有节点ID
const getAllNodeIds = (nodes: PermissionTreeItem[]): number[] => {
  const ids: number[] = [];
  const traverse = (list: PermissionTreeItem[]) => {
    for (const item of list) {
      ids.push(item.id);
      if (item.children && item.children.length > 0) {
        traverse(item.children);
      }
    }
  };
  traverse(nodes);
  return ids;
};

// 展开/折叠全部
const handleExpandAll = (expand: boolean) => {
  if (expand) {
    expandedPermKeys.value = getAllNodeIds(permTreeData.value);
  } else {
    expandedPermKeys.value = [];
  }
};

// 全选/清空
const handleSelectAll = (select: boolean) => {
  if (select) {
    checkedPermKeys.value = getAllNodeIds(permTreeData.value);
    halfCheckedPermKeys.value = [];
  } else {
    checkedPermKeys.value = [];
    halfCheckedPermKeys.value = [];
  }
};

// 树选中变化回调
const onTreeCheck = (checked: any, e: any) => {
  if (Array.isArray(checked)) {
    checkedPermKeys.value = checked;
  } else if (checked?.checked) {
    checkedPermKeys.value = checked.checked;
  }
  if (e?.halfCheckedKeys) {
    halfCheckedPermKeys.value = e.halfCheckedKeys;
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await listRolesApi();
    if (res.code === 200 && res.data) {
      roleList.value = res.data;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
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

// 打开权限配置弹窗
const openPermModal = async (record: RoleItem) => {
  currentRole.value = record;
  permModalVisible.value = true;
  loadingPermTree.value = true;
  checkedPermKeys.value = [];
  halfCheckedPermKeys.value = [];

  try {
    // 1. 获取权限树
    if (permTreeData.value.length === 0) {
      const treeRes = await getPermissionTreeApi();
      if (treeRes.code === 200 && treeRes.data) {
        permTreeData.value = treeRes.data;
      }
    }
    // 默认展开所有一级和二级目录
    expandedPermKeys.value = getAllNodeIds(permTreeData.value);

    // 2. 获取该角色当前已分配的权限列表
    if (record.id) {
      const rolePermRes = await getRolePermissionIdsApi(record.id);
      if (rolePermRes.code === 200 && rolePermRes.data) {
        checkedPermKeys.value = rolePermRes.data;
      }
    }
  } catch (err: any) {
    message.error(err?.message || '获取权限数据失败');
  } finally {
    loadingPermTree.value = false;
  }
};

// 保存角色权限分配
const handleSavePerms = async () => {
  if (!currentRole.value?.id) return;
  try {
    savingPerms.value = true;
    // 联合 checked 与 halfChecked 保证上级目录访问权
    const allIds = Array.from(new Set([
      ...checkedPermKeys.value,
      ...halfCheckedPermKeys.value
    ]));

    const res = await assignRolePermissionsApi({
      roleId: currentRole.value.id,
      permissionIds: allIds
    });
    if (res.code === 200) {
      message.success(`角色 [${currentRole.value.roleName}] 权限分配已成功保存并实时生效`);
      permModalVisible.value = false;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    savingPerms.value = false;
  }
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

.perm-tree-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
}

.perm-count-badge {
  font-size: 13px;
  color: #64748b;
}

.perm-count-badge b {
  color: #1677ff;
}

.perm-tree-container {
  max-height: 420px;
  overflow-y: auto;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.perm-tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.perm-node-name {
  font-size: 14px;
  color: #1e293b;
}

.perm-node-code {
  font-size: 11px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
}
</style>
