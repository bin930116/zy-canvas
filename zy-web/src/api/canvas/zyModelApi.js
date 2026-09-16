import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/model/` + url, ...arg)

/**
 * 模型配置Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:27
 **/
export default {
	// 获取模型配置分页
	zyModelPage(data) {
		return request('page', data, 'get')
	},
	// 提交模型配置表单 edit为true时为编辑，默认为新增
	zyModelSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除模型配置
	zyModelDelete(data) {
		return request('delete', data)
	},
	// 获取模型配置详情
	zyModelDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载模型配置导入模板
    zyModelDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入模型配置
	zyModelImport(data) {
		return request('importData', data)
	},
	// 导出模型配置
	zyModelExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
