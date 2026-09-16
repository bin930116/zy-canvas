import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/share/` + url, ...arg)

/**
 * 画布项目分享Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:56
 **/
export default {
	// 获取画布项目分享分页
	zySharePage(data) {
		return request('page', data, 'get')
	},
	// 提交画布项目分享表单 edit为true时为编辑，默认为新增
	zyShareSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除画布项目分享
	zyShareDelete(data) {
		return request('delete', data)
	},
	// 获取画布项目分享详情
	zyShareDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载画布项目分享导入模板
    zyShareDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入画布项目分享
	zyShareImport(data) {
		return request('importData', data)
	},
	// 导出画布项目分享
	zyShareExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
