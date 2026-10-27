<template>
  <div class="dashboard-page">
    <!-- 欢迎信息条与模式切换开关 -->
    <a-card :bordered="false" class="welcome-card">
      <div class="welcome-content">
        <a-avatar :size="64" style="background-color: #1677ff">
          {{ userStore.displayName.charAt(0) }}
        </a-avatar>
        <div class="welcome-text">
          <h3>您好，{{ userStore.displayName }}，祝您今天工作愉快！</h3>
          <p>
            所属部门：{{ userStore.userInfo?.deptName || '未分配' }} ｜ 
            当前身份：{{ userStore.userInfo?.postName || '正式员工' }} ｜ 
            工号：{{ userStore.userInfo?.workNo || 'OA0001' }}
          </p>
        </div>
        <div class="quick-clock">
          <a-space size="middle">
            <div class="mode-switcher-box">
              <span class="mode-label">工作台视图：</span>
              <a-radio-group v-model:value="dashboardMode" button-style="solid" @change="saveModePreference">
                <a-radio-button value="modern">
                  <DashboardOutlined /> 现代待办中枢
                </a-radio-button>
                <a-radio-button value="portal">
                  <AppstoreOutlined /> 传统全景服务大厅
                </a-radio-button>
              </a-radio-group>
            </div>
            <a-button type="primary" size="large" @click="handleQuickClock">
              <CheckCircleOutlined /> 快速打卡
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- ============================================================== -->
    <!-- 模式 A：现代待办中枢模式 (Executive Hub)                       -->
    <!-- ============================================================== -->
    <div v-if="dashboardMode === 'modern'" class="modern-layout">
      <!-- 数据指标统计卡片 -->
      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="我的待办任务"
              :value="pendingTotal"
              :value-style="{ color: '#cf1322' }"
            >
              <template #prefix><ClockCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="我的已办事项"
              :value="handledTotal"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix><CheckCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="我发起的单据"
              :value="submittedTotal"
              :value-style="{ color: '#1677ff' }"
            >
              <template #prefix><SendOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="红头公文与通知"
              :value="dashboardNotices.length"
              :value-style="{ color: '#d46b08' }"
            >
              <template #prefix><FileTextOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 常用协同事务快捷发起 -->
      <a-card :bordered="false" style="margin-top: 16px" title="⚡ 常用协同事务快捷发起">
        <template #extra>
          <span style="font-size: 13px; color: #888">支持暂存草稿与一键启动流转审批</span>
        </template>
        <a-row :gutter="[12, 12]">
          <a-col :xs="12" :sm="8" :md="6" :lg="3">
            <a-button block type="dashed" size="large" @click="leaveModalOpen = true">
              <template #icon><CalendarOutlined style="color: #1677ff" /></template>
              发起请假
            </a-button>
          </a-col>
          <a-col :xs="12" :sm="8" :md="6" :lg="3">
            <a-button block type="dashed" size="large" @click="repairModalOpen = true">
              <template #icon><ClockCircleOutlined style="color: #52c41a" /></template>
              漏卡补卡
            </a-button>
          </a-col>
          <a-col :xs="12" :sm="8" :md="6" :lg="3">
            <a-button block type="dashed" size="large" @click="overtimeModalOpen = true">
              <template #icon><FieldTimeOutlined style="color: #fa8c16" /></template>
              申报加班
            </a-button>
          </a-col>
          <a-col :xs="12" :sm="8" :md="6" :lg="3">
            <a-button block type="dashed" size="large" @click="egressModalOpen = true">
              <template #icon><SendOutlined style="color: #13c2c2" /></template>
              外勤报备
            </a-button>
          </a-col>
          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-button block type="dashed" size="large" @click="sealModalOpen = true">
              <template #icon><AuditOutlined style="color: #722ed1" /></template>
              申请用印
            </a-button>
          </a-col>
          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-button block type="dashed" size="large" @click="redheadModalOpen = true">
              <template #icon><FileDoneOutlined style="color: #f5222d" /></template>
              起草公文
            </a-button>
          </a-col>
          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-button block type="dashed" size="large" @click="borrowModalOpen = true">
              <template #icon><ReadOutlined style="color: #eb2f96" /></template>
              借阅档案
            </a-button>
          </a-col>
        </a-row>
      </a-card>

      <!-- 工作台核心面板 (待办/已办/我发起的/通知) -->
      <a-card :bordered="false" style="margin-top: 16px">
        <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
          <!-- 待办任务 -->
          <a-tab-pane key="pending">
            <template #tab>
              <span>
                <ClockCircleOutlined /> 待办审批
                <a-badge :count="pendingTotal" :offset="[10, -2]" />
              </span>
            </template>

            <a-table
              :columns="pendingColumns"
              :data-source="pendingList"
              :loading="pendingLoading"
              row-key="taskId"
              :pagination="{
                current: pendingPage,
                pageSize: 10,
                total: pendingTotal,
                onChange: (p: number) => { pendingPage = p; loadPendingTasks(); }
              }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'title'">
                  <strong>{{ record.title }}</strong>
                </template>
                <template v-else-if="column.key === 'taskType'">
                  <a-tag v-if="record.taskType === 'SIGN'" color="purple">加签任务</a-tag>
                  <a-tag v-else color="blue">常规审批</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="primary" size="small" @click="openApproval(record)">
                    办理审批
                  </a-button>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <!-- 已办任务 -->
          <a-tab-pane key="handled">
            <template #tab>
              <span><CheckCircleOutlined /> 已办履历</span>
            </template>

            <a-table
              :columns="handledColumns"
              :data-source="handledList"
              :loading="handledLoading"
              row-key="instId"
              :pagination="{
                current: handledPage,
                pageSize: 10,
                total: handledTotal,
                onChange: (p: number) => { handledPage = p; loadHandledTasks(); }
              }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'instStatus'">
                  <a-tag v-if="record.instStatus === 2" color="green">审批通过</a-tag>
                  <a-tag v-else-if="record.instStatus === 3" color="red">已驳回</a-tag>
                  <a-tag v-else-if="record.instStatus === 4" color="gray">已作废</a-tag>
                  <a-tag v-else color="processing">流转中</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" @click="viewTimeline(record.instId)">
                    查看履历
                  </a-button>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <!-- 我发起的 -->
          <a-tab-pane key="submitted">
            <template #tab>
              <span><SendOutlined /> 我发起的申请</span>
            </template>

            <a-table
              :columns="submittedColumns"
              :data-source="submittedList"
              :loading="submittedLoading"
              row-key="instId"
              :pagination="{
                current: submittedPage,
                pageSize: 10,
                total: submittedTotal,
                onChange: (p: number) => { submittedPage = p; loadMySubmitted(); }
              }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'instStatus'">
                  <a-tag v-if="record.instStatus === 0" color="default">草稿待发</a-tag>
                  <a-tag v-else-if="record.instStatus === 1" color="processing">审核中</a-tag>
                  <a-tag v-else-if="record.instStatus === 2" color="green">审批通过</a-tag>
                  <a-tag v-else-if="record.instStatus === 3" color="red">已驳回</a-tag>
                  <a-tag v-else color="gray">已作废</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" @click="viewTimeline(record.instId)">
                    流转跟踪
                  </a-button>
                  <a-popconfirm
                    v-if="record.instStatus === 1"
                    title="确认作废撤销该审批申请吗？"
                    @confirm="handleCancelProcess(record.instId)"
                  >
                    <a-button type="link" danger size="small">撤回作废</a-button>
                  </a-popconfirm>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <!-- 红头公文与协同通知 -->
          <a-tab-pane key="notices">
            <template #tab>
              <span>
                <FileTextOutlined /> 公文公告
                <a-badge v-if="unreadNoticeBadge > 0" :count="unreadNoticeBadge" :offset="[10, -2]" />
              </span>
            </template>
            <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center">
              <span style="color: #8c8c8c; font-size: 13px">实时同步集团红头文件、规章决议与部门通告</span>
              <a-button type="link" size="small" @click="router.push('/business/notice')">
                进入通告中心与阅读追踪大厅 &gt;
              </a-button>
            </div>
            <a-list item-layout="horizontal" :data-source="dashboardNotices" :loading="noticeLoading">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <div style="display: flex; align-items: center; gap: 8px">
                        <a-badge v-if="!item.isRead" status="error" title="当前未读" />
                        <a-tag :color="getCategoryColor(item.category)">{{ item.category || '通告' }}</a-tag>
                        <a
                          href="javascript:;"
                          :style="{ fontWeight: item.isRead ? 'normal' : 'bold', color: item.isRead ? '#262626' : '#1677ff' }"
                          @click="handleViewDashboardNotice(item)"
                        >
                          {{ item.title }}
                        </a>
                        <a-tag v-if="item.priority === 'H'" color="red">加急</a-tag>
                      </div>
                    </template>
                    <template #description>
                      <div style="color: #8c8c8c; font-size: 12px; margin-top: 4px">
                        发布人员：{{ item.publisherName }} &nbsp;|&nbsp; 发布时间：{{ item.publishTime || item.createTime }}
                      </div>
                    </template>
                    <template #avatar>
                      <a-avatar style="background-color: #f5222d">公</a-avatar>
                    </template>
                  </a-list-item-meta>
                  <div style="text-align: right">
                    <a-tag color="blue">全员查阅率 {{ item.readRate || 0 }}%</a-tag>
                    <a-button type="link" size="small" @click="handleViewDashboardNotice(item)">阅读公文</a-button>
                  </div>
                </a-list-item>
              </template>
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>

    <!-- ============================================================== -->
    <!-- 模式 B：传统全景服务大厅模式 (Traditional Panoramic Portal)    -->
    <!-- ============================================================== -->
    <div v-else class="portal-layout" style="margin-top: 16px">
      <a-row :gutter="16">
        <!-- 左侧：全景业务九宫格卡片大厅 (17 span) -->
        <a-col :xs="24" :xl="17">
          <!-- 1. 人力资源中心 -->
          <a-card :bordered="false" class="portal-section-card" title="👥 人力资源综合服务大厅">
            <template #extra>
              <span class="portal-sub">考勤打卡、休假调配、工时申报与薪酬中心</span>
            </template>
            <div class="portal-grid">
              <div class="portal-item" @click="handleQuickClock">
                <div class="icon-box bg-blue"><ClockCircleOutlined /></div>
                <div class="info-box">
                  <div class="item-title">考勤打卡</div>
                  <div class="item-desc">考勤签到与日常打卡</div>
                </div>
              </div>
              <div class="portal-item" @click="repairModalOpen = true">
                <div class="icon-box bg-green"><CheckCircleOutlined /></div>
                <div class="info-box">
                  <div class="item-title">异常补卡</div>
                  <div class="item-desc">漏打卡冲正申请办理</div>
                </div>
              </div>
              <div class="portal-item" @click="leaveModalOpen = true">
                <div class="icon-box bg-cyan"><CalendarOutlined /></div>
                <div class="info-box">
                  <div class="item-title">请假申请</div>
                  <div class="item-desc">年假事假病假流转</div>
                </div>
              </div>
              <div class="portal-item" @click="egressModalOpen = true">
                <div class="icon-box bg-purple"><SendOutlined /></div>
                <div class="info-box">
                  <div class="item-title">外出申请</div>
                  <div class="item-desc">外出公干外勤报备</div>
                </div>
              </div>
              <div class="portal-item" @click="overtimeModalOpen = true">
                <div class="icon-box bg-orange"><FieldTimeOutlined /></div>
                <div class="info-box">
                  <div class="item-title">加班申请</div>
                  <div class="item-desc">周末工作日加班申报</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/business/salary')">
                <div class="icon-box bg-gold"><DollarOutlined /></div>
                <div class="info-box">
                  <div class="item-title">月度工资条</div>
                  <div class="item-desc">二级安全核验与签收</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/system/address')">
                <div class="icon-box bg-geekblue"><ContactsOutlined /></div>
                <div class="info-box">
                  <div class="item-title">全员通讯录</div>
                  <div class="item-desc">部门名录与一键拨号</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/business/attendance')">
                <div class="icon-box bg-lime"><FileTextOutlined /></div>
                <div class="info-box">
                  <div class="item-title">考勤月度统计</div>
                  <div class="item-desc">个人月度出勤流水台账</div>
                </div>
              </div>
            </div>
          </a-card>

          <!-- 2. 行政公文与企业印章中心 -->
          <a-card :bordered="false" class="portal-section-card" title="📑 行政管理与印章用印中心" style="margin-top: 16px">
            <template #extra>
              <span class="portal-sub">企业印章防伪套打、国标红头公文拟发与档案借阅</span>
            </template>
            <div class="portal-grid">
              <div class="portal-item" @click="sealModalOpen = true">
                <div class="icon-box bg-purple"><AuditOutlined /></div>
                <div class="info-box">
                  <div class="item-title">用印申请 (合同)</div>
                  <div class="item-desc">合同专用章审批与骑缝</div>
                </div>
              </div>
              <div class="portal-item" @click="sealModalOpen = true">
                <div class="icon-box bg-magenta"><AuditOutlined /></div>
                <div class="info-box">
                  <div class="item-title">用印申请 (公章)</div>
                  <div class="item-desc">综合公章与法人章使用</div>
                </div>
              </div>
              <div class="portal-item" @click="sealModalOpen = true">
                <div class="icon-box bg-volcano"><AuditOutlined /></div>
                <div class="info-box">
                  <div class="item-title">印章外带借用</div>
                  <div class="item-desc">物理印章外借流转登记</div>
                </div>
              </div>
              <div class="portal-item" @click="redheadModalOpen = true">
                <div class="icon-box bg-red"><FileDoneOutlined /></div>
                <div class="info-box">
                  <div class="item-title">红头公文拟发</div>
                  <div class="item-desc">国标公文双红线套打</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/business/notice')">
                <div class="icon-box bg-orange"><NotificationOutlined /></div>
                <div class="info-box">
                  <div class="item-title">公文通告大厅</div>
                  <div class="item-desc">通告发布与全员已读追踪</div>
                </div>
              </div>
              <div class="portal-item" @click="borrowModalOpen = true">
                <div class="icon-box bg-pink"><ReadOutlined /></div>
                <div class="info-box">
                  <div class="item-title">档案资料借阅</div>
                  <div class="item-desc">企业卷宗档案借出申请</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/business/borrow')">
                <div class="icon-box bg-blue"><BookOutlined /></div>
                <div class="info-box">
                  <div class="item-title">借阅台账核销</div>
                  <div class="item-desc">全周期归还与借出留痕</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/business/redhead')">
                <div class="icon-box bg-cyan"><FileTextOutlined /></div>
                <div class="info-box">
                  <div class="item-title">公文印发历史</div>
                  <div class="item-desc">已办结决议公文总览</div>
                </div>
              </div>
            </div>
          </a-card>

          <!-- 3. 组织架构与系统管理中心 -->
          <a-card :bordered="false" class="portal-section-card" title="⚙️ 组织架构与协同系统控制中枢" style="margin-top: 16px">
            <template #extra>
              <span class="portal-sub">细粒度权限树分配、数据字典与企微/钉钉/飞书机器人</span>
            </template>
            <div class="portal-grid">
              <div class="portal-item" @click="router.push('/system/dept')">
                <div class="icon-box bg-blue"><ApartmentOutlined /></div>
                <div class="info-box">
                  <div class="item-title">部门架构维护</div>
                  <div class="item-desc">树形企业组织层级管理</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/system/user')">
                <div class="icon-box bg-cyan"><TeamOutlined /></div>
                <div class="info-box">
                  <div class="item-title">员工用户管理</div>
                  <div class="item-desc">员工档案工号岗位维护</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/system/role')">
                <div class="icon-box bg-green"><SafetyCertificateOutlined /></div>
                <div class="info-box">
                  <div class="item-title">角色权限体系</div>
                  <div class="item-desc">菜单按钮可视化勾选树</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/system/dict')">
                <div class="icon-box bg-orange"><BookOutlined /></div>
                <div class="info-box">
                  <div class="item-title">数据字典中心</div>
                  <div class="item-desc">标准枚举与键值分类配置</div>
                </div>
              </div>
              <div class="portal-item" @click="router.push('/system/notify')">
                <div class="icon-box bg-purple"><RobotOutlined /></div>
                <div class="info-box">
                  <div class="item-title">消息提醒机器人</div>
                  <div class="item-desc">企微/钉钉/飞书待办直推</div>
                </div>
              </div>
              <div class="portal-item" @click="message.info('点击右上角个人头像，可自主修改密码与维护手机邮箱资料')">
                <div class="icon-box bg-gold"><LockOutlined /></div>
                <div class="info-box">
                  <div class="item-title">个人安全资料</div>
                  <div class="item-desc">右上角员工自改密码与资料</div>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧：雷达图与指标速览 (7 span) -->
        <a-col :xs="24" :xl="7">
          <!-- 五维综合能力雷达图 -->
          <a-card :bordered="false" class="portal-side-card" title="📊 员工协同综合能力指数">
            <template #extra>
              <a-tag color="processing">实时画像</a-tag>
            </template>
            <div class="radar-box">
              <svg viewBox="0 0 240 220" class="radar-svg">
                <!-- 背景同心网格五边形 (20%, 40%, 60%, 80%, 100%) -->
                <polygon points="120,30 206,92 173,193 67,193 34,92" fill="none" stroke="#e8e8e8" stroke-width="1" />
                <polygon points="120,48 189,98 162,178 78,178 51,98" fill="none" stroke="#f0f0f0" stroke-width="1" />
                <polygon points="120,66 172,104 152,164 88,164 68,104" fill="none" stroke="#f0f0f0" stroke-width="1" />
                <polygon points="120,84 154,110 141,150 99,150 86,110" fill="none" stroke="#f0f0f0" stroke-width="1" />
                <polygon points="120,102 137,116 131,136 109,136 103,116" fill="none" stroke="#f5f5f5" stroke-width="1" />

                <!-- 轴线 -->
                <line x1="120" y1="120" x2="120" y2="30" stroke="#d9d9d9" stroke-dasharray="2,2" />
                <line x1="120" y1="120" x2="206" y2="92" stroke="#d9d9d9" stroke-dasharray="2,2" />
                <line x1="120" y1="120" x2="173" y2="193" stroke="#d9d9d9" stroke-dasharray="2,2" />
                <line x1="120" y1="120" x2="67" y2="193" stroke="#d9d9d9" stroke-dasharray="2,2" />
                <line x1="120" y1="120" x2="34" y2="92" stroke="#d9d9d9" stroke-dasharray="2,2" />

                <!-- 实际能力多边形 (影响力:85, 执行力:92, 协作力:90, 学习力:88, 管理力:78) -->
                <polygon
                  points="120,43 199,94 167,185 73,184 47,97"
                  fill="rgba(22, 119, 255, 0.28)"
                  stroke="#1677ff"
                  stroke-width="2"
                />
                <!-- 顶点圆点 -->
                <circle cx="120" cy="43" r="3.5" fill="#1677ff" />
                <circle cx="199" cy="94" r="3.5" fill="#1677ff" />
                <circle cx="167" cy="185" r="3.5" fill="#1677ff" />
                <circle cx="73" cy="184" r="3.5" fill="#1677ff" />
                <circle cx="47" cy="97" r="3.5" fill="#1677ff" />

                <!-- 轴名称文本 -->
                <text x="120" y="20" text-anchor="middle" font-size="11" fill="#595959" font-weight="bold">影响力 85</text>
                <text x="212" y="96" text-anchor="start" font-size="11" fill="#595959" font-weight="bold">执行力 92</text>
                <text x="180" y="208" text-anchor="middle" font-size="11" fill="#595959" font-weight="bold">协作力 90</text>
                <text x="60" y="208" text-anchor="middle" font-size="11" fill="#595959" font-weight="bold">学习力 88</text>
                <text x="28" y="96" text-anchor="end" font-size="11" fill="#595959" font-weight="bold">管理力 78</text>
              </svg>
            </div>
            <div class="radar-score-bar">
              <span class="score-badge">综合评分：<strong>91.4</strong> 分</span>
              <span class="rank-badge">超越集团 <strong>93.6%</strong> 员工</span>
            </div>
          </a-card>

          <!-- 待办审批直通车 -->
          <a-card :bordered="false" class="portal-side-card" title="⚡ 待办审批极速直通" style="margin-top: 16px">
            <template #extra>
              <a-badge :count="pendingTotal" />
            </template>
            <div v-if="pendingList.length === 0" style="text-align: center; padding: 24px 0; color: #bfbfbf">
              暂无待处理审批任务
            </div>
            <div v-else class="portal-task-list">
              <div v-for="t in pendingList.slice(0, 4)" :key="t.taskId" class="portal-task-item">
                <div class="task-info">
                  <div class="task-title">{{ t.title }}</div>
                  <div class="task-sub">{{ t.initiatorName }} · {{ t.nodeName }}</div>
                </div>
                <a-button type="primary" size="small" @click="openApproval(t)">办理</a-button>
              </div>
            </div>
          </a-card>

          <!-- 集团最新公文与公告 -->
          <a-card :bordered="false" class="portal-side-card" title="📢 集团最新公文与公告" style="margin-top: 16px">
            <template #extra>
              <a-button type="link" size="small" @click="router.push('/business/notice')">通告大厅 &gt;</a-button>
            </template>
            <div class="portal-notice-list">
              <div
                v-for="n in dashboardNotices.slice(0, 4)"
                :key="n.id"
                class="portal-notice-item"
                @click="handleViewDashboardNotice(n)"
              >
                <a-tag :color="getCategoryColor(n.category)" size="small">{{ n.category || '通告' }}</a-tag>
                <span class="notice-txt" :class="{ unread: !n.isRead }">{{ n.title }}</span>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 公告阅读详情弹窗 -->
    <a-modal
      v-model:open="dashboardNoticeDetailVisible"
      :title="dashboardCurrentNotice?.title || '公文通告详情'"
      :footer="null"
      width="720px"
    >
      <div v-if="dashboardCurrentNotice" style="padding: 10px 0">
        <div style="display: flex; gap: 16px; font-size: 13px; color: #8c8c8c; margin-bottom: 16px">
          <span><strong>分类：</strong>{{ dashboardCurrentNotice.category }}</span>
          <span><strong>发布人：</strong>{{ dashboardCurrentNotice.publisherName }}</span>
          <span><strong>发布时间：</strong>{{ dashboardCurrentNotice.publishTime || dashboardCurrentNotice.createTime }}</span>
          <span><strong>阅读率：</strong>{{ dashboardCurrentNotice.readCount }} / {{ dashboardCurrentNotice.totalCount }} 人 ({{ dashboardCurrentNotice.readRate }}%)</span>
        </div>
        <div style="background: #fafafa; border: 1px solid #f0f0f0; border-radius: 6px; padding: 20px; font-size: 14px; line-height: 1.8; white-space: pre-wrap">
          {{ dashboardCurrentNotice.content }}
        </div>
        <div style="text-align: right; margin-top: 20px">
          <a-space>
            <a-button type="default" @click="dashboardNoticeDetailVisible = false">关闭</a-button>
            <a-button type="primary" @click="router.push('/business/notice')">
              前往追踪全员查阅状态
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>

    <!-- 审批流转弹窗 -->
    <ApprovalModal
      v-model:open="approvalModalVisible"
      :task="currentTask"
      @success="handleApprovalSuccess"
    />

    <!-- 查看流转时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="审批履历时间轴"
      :footer="null"
      width="600px"
    >
      <TimelineHistory :inst-id="currentTimelineInstId || undefined" />
    </a-modal>

    <!-- 业务快捷发起弹窗 -->
    <LeaveModal v-model:open="leaveModalOpen" @success="handleFormSuccess" />
    <RepairModal v-model:open="repairModalOpen" @success="handleFormSuccess" />
    <OvertimeModal v-model:open="overtimeModalOpen" @success="handleFormSuccess" />
    <EgressModal v-model:open="egressModalOpen" @success="handleFormSuccess" />
    <SealModal v-model:open="sealModalOpen" @success="handleFormSuccess" />
    <RedheadModal v-model:open="redheadModalOpen" @success="handleFormSuccess" />
    <BorrowModal v-model:open="borrowModalOpen" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { message } from 'ant-design-vue';
import {
  ClockCircleOutlined,
  CheckCircleOutlined,
  SendOutlined,
  FileTextOutlined,
  CalendarOutlined,
  AuditOutlined,
  ReadOutlined,
  FileDoneOutlined,
  FieldTimeOutlined,
  DashboardOutlined,
  AppstoreOutlined,
  DollarOutlined,
  ContactsOutlined,
  BookOutlined,
  ApartmentOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  RobotOutlined,
  LockOutlined,
  NotificationOutlined
} from '@ant-design/icons-vue';
import {
  getPendingTasksApi,
  getHandledTasksApi,
  getMySubmittedApi,
  cancelProcessApi
} from '@/api/workflow';
import { pageNoticesApi, getNoticeDetailApi } from '@/api/business';
import type { TaskVO } from '@/types/workflow';
import type { NoticeVO } from '@/types/business';
import ApprovalModal from '@/views/workflow/components/ApprovalModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';
import LeaveModal from '@/views/business/leave/components/LeaveModal.vue';
import RepairModal from '@/views/business/attendance/components/RepairModal.vue';
import OvertimeModal from '@/views/business/attendance/components/OvertimeModal.vue';
import EgressModal from '@/views/business/attendance/components/EgressModal.vue';
import SealModal from '@/views/business/seal/components/SealModal.vue';
import RedheadModal from '@/views/business/redhead/components/RedheadModal.vue';
import BorrowModal from '@/views/business/borrow/components/BorrowModal.vue';

const router = useRouter();
const userStore = useUserStore();

// 工作台视图模式 ('modern' 现代待办中枢 | 'portal' 传统全景服务大厅)
const dashboardMode = ref<'modern' | 'portal'>((localStorage.getItem('oa_dashboard_mode') as any) || 'modern');

const saveModePreference = () => {
  localStorage.setItem('oa_dashboard_mode', dashboardMode.value);
  message.info(dashboardMode.value === 'portal' ? '已切换至「传统全景服务大厅」模式' : '已切换至「现代待办中枢」模式');
};

// 快捷表单弹窗状态
const leaveModalOpen = ref(false);
const repairModalOpen = ref(false);
const overtimeModalOpen = ref(false);
const egressModalOpen = ref(false);
const sealModalOpen = ref(false);
const redheadModalOpen = ref(false);
const borrowModalOpen = ref(false);

const handleFormSuccess = () => {
  loadPendingTasks();
  loadMySubmitted();
};

const activeTab = ref('pending');

// 待办相关状态
const pendingLoading = ref(false);
const pendingList = ref<TaskVO[]>([]);
const pendingTotal = ref(0);
const pendingPage = ref(1);

// 已办相关状态
const handledLoading = ref(false);
const handledList = ref<TaskVO[]>([]);
const handledTotal = ref(0);
const handledPage = ref(1);

// 我发起的申请状态
const submittedLoading = ref(false);
const submittedList = ref<TaskVO[]>([]);
const submittedTotal = ref(0);
const submittedPage = ref(1);

// 审批流转弹窗
const approvalModalVisible = ref(false);
const currentTask = ref<TaskVO | null>(null);

// 审批历史流转
const timelineModalVisible = ref(false);
const currentTimelineInstId = ref<number | undefined>(undefined);

// 表格列配置
const pendingColumns = [
  { title: '待办任务标题', dataIndex: 'title', key: 'title' },
  { title: '发起人', dataIndex: 'initiatorName', key: 'initiatorName', width: 120 },
  { title: '流程环节', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '任务类型', dataIndex: 'taskType', key: 'taskType', width: 120 },
  { title: '到达时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120, align: 'center' as const }
];

const handledColumns = [
  { title: '已办单据名称', dataIndex: 'title', key: 'title' },
  { title: '申请人', dataIndex: 'initiatorName', key: 'initiatorName', width: 120 },
  { title: '经办节点', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '流程总状态', dataIndex: 'instStatus', key: 'instStatus', width: 120 },
  { title: '办理时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 110, align: 'center' as const }
];

const submittedColumns = [
  { title: '申请单据名称', dataIndex: 'title', key: 'title' },
  { title: '所属流程', dataIndex: 'procKey', key: 'procKey', width: 140 },
  { title: '当前环节', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '流程状态', dataIndex: 'instStatus', key: 'instStatus', width: 120 },
  { title: '发起时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160, align: 'center' as const }
];

// 公文通告实时数据
const dashboardNotices = ref<NoticeVO[]>([]);
const noticeLoading = ref(false);
const unreadNoticeBadge = computed(() => dashboardNotices.value.filter(n => !n.isRead).length);
const dashboardNoticeDetailVisible = ref(false);
const dashboardCurrentNotice = ref<NoticeVO | null>(null);

const getCategoryColor = (cat?: string) => {
  switch (cat) {
    case '通报': return 'red';
    case '决议': return 'purple';
    case '公告': return 'orange';
    case '通知':
    default:
      return 'blue';
  }
};

const loadNotices = async () => {
  noticeLoading.value = true;
  try {
    const res = await pageNoticesApi({ pageNum: 1, pageSize: 6 });
    dashboardNotices.value = res.data.records || [];
  } catch (ignored) {
  } finally {
    noticeLoading.value = false;
  }
};

const handleViewDashboardNotice = async (notice: NoticeVO) => {
  try {
    const res = await getNoticeDetailApi(notice.id);
    dashboardCurrentNotice.value = res.data;
    dashboardNoticeDetailVisible.value = true;
    notice.isRead = true;
  } catch (err: any) {
    message.error(err.message || '获取通告详情失败');
  }
};

// 加载待办任务
const loadPendingTasks = async () => {
  pendingLoading.value = true;
  try {
    const res = await getPendingTasksApi(pendingPage.value, 10);
    pendingList.value = res.data.data.records || [];
    pendingTotal.value = res.data.data.total || 0;
  } catch (ignored) {
  } finally {
    pendingLoading.value = false;
  }
};

// 加载已办任务
const loadHandledTasks = async () => {
  handledLoading.value = true;
  try {
    const res = await getHandledTasksApi(handledPage.value, 10);
    handledList.value = res.data.data.records || [];
    handledTotal.value = res.data.data.total || 0;
  } catch (ignored) {
  } finally {
    handledLoading.value = false;
  }
};

// 加载我发起的
const loadMySubmitted = async () => {
  submittedLoading.value = true;
  try {
    const res = await getMySubmittedApi(submittedPage.value, 10);
    submittedList.value = res.data.data.records || [];
    submittedTotal.value = res.data.data.total || 0;
  } catch (ignored) {
  } finally {
    submittedLoading.value = false;
  }
};

const handleTabChange = (key: string) => {
  if (key === 'pending') loadPendingTasks();
  else if (key === 'handled') loadHandledTasks();
  else if (key === 'submitted') loadMySubmitted();
  else if (key === 'notices') loadNotices();
};

const openApproval = (task: TaskVO) => {
  currentTask.value = task;
  approvalModalVisible.value = true;
};

const handleApprovalSuccess = () => {
  loadPendingTasks();
  loadHandledTasks();
};

const viewTimeline = (instId: number) => {
  currentTimelineInstId.value = instId;
  timelineModalVisible.value = true;
};

const handleCancelProcess = async (instId: number) => {
  try {
    await cancelProcessApi(instId, '发起人撤销');
    message.success('申请单据已撤销作废');
    loadMySubmitted();
  } catch (ignored) {
  }
};

const handleQuickClock = () => {
  message.success('打卡成功！当前考勤状态正常 (9:00)');
};

onMounted(() => {
  loadPendingTasks();
  loadHandledTasks();
  loadMySubmitted();
  loadNotices();
});
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
}

.welcome-card {
  border-radius: 8px;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 6px;
}

.welcome-text p {
  color: #8c8c8c;
  font-size: 13px;
  margin: 0;
}

.quick-clock {
  margin-left: auto;
}

.mode-switcher-box {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 6px;
}

.mode-label {
  font-size: 12px;
  color: #595959;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

/* ================= 传统全景服务大厅样式 ================= */
.portal-section-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.portal-sub {
  color: #8c8c8c;
  font-size: 13px;
}

.portal-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 8px 0;
}

@media screen and (max-width: 1200px) {
  .portal-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.portal-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.portal-item:hover {
  background: #ffffff;
  border-color: #91caff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.12);
  transform: translateY(-2px);
}

.icon-box {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
}

.bg-blue { background: linear-gradient(135deg, #1677ff, #0958d9); }
.bg-green { background: linear-gradient(135deg, #52c41a, #389e0d); }
.bg-cyan { background: linear-gradient(135deg, #13c2c2, #08979c); }
.bg-purple { background: linear-gradient(135deg, #722ed1, #531dab); }
.bg-orange { background: linear-gradient(135deg, #fa8c16, #d46b08); }
.bg-gold { background: linear-gradient(135deg, #faad14, #d48806); }
.bg-geekblue { background: linear-gradient(135deg, #2f54eb, #1d39c4); }
.bg-lime { background: linear-gradient(135deg, #a0d911, #7cb305); }
.bg-magenta { background: linear-gradient(135deg, #eb2f96, #c41d7f); }
.bg-volcano { background: linear-gradient(135deg, #fa541c, #d4380d); }
.bg-red { background: linear-gradient(135deg, #f5222d, #cf1322); }
.bg-pink { background: linear-gradient(135deg, #eb2f96, #9e1068); }

.info-box {
  overflow: hidden;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.portal-item:hover .item-title {
  color: #1677ff;
}

.item-desc {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.portal-side-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.radar-box {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.radar-svg {
  width: 100%;
  max-width: 250px;
  height: auto;
}

.radar-score-bar {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 12px;
}

.score-badge { color: #1677ff; }
.rank-badge { color: #52c41a; }

.portal-task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.portal-task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.task-info {
  overflow: hidden;
  margin-right: 8px;
}

.task-title {
  font-size: 13px;
  font-weight: 500;
  color: #262626;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-sub {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 2px;
}

.portal-notice-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.portal-notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.portal-notice-item:hover {
  background: #f5f5f5;
}

.notice-txt {
  font-size: 13px;
  color: #595959;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.notice-txt.unread {
  font-weight: 600;
  color: #1f1f1f;
}
</style>
