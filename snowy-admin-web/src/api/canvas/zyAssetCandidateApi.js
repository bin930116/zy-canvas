import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/assetcandidate/` + url, ...arg)

/**
 * 短剧项目资产候选Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/08 14:40
 **/
export default {
	// 获取短剧项目资产候选分页
	zyAssetCandidatePage(data) {
		return request('page', data, 'get')
	},
	// 提交短剧项目资产候选表单 edit为true时为编辑，默认为新增
	zyAssetCandidateSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除短剧项目资产候选
	zyAssetCandidateDelete(data) {
		return request('delete', data)
	},
	// 获取短剧项目资产候选详情
	zyAssetCandidateDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载短剧项目资产候选导入模板
    zyAssetCandidateDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入短剧项目资产候选
	zyAssetCandidateImport(data) {
		return request('importData', data)
	},
	// 导出短剧项目资产候选
	zyAssetCandidateExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
