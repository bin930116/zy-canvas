import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/asset/` + url, ...arg)

/**
 * 短剧项目资产Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/08 14:46
 **/
export default {
	// 获取短剧项目资产分页
	zyAssetPage(data) {
		return request('page', data, 'get')
	},
	// 提交短剧项目资产表单 edit为true时为编辑，默认为新增
	zyAssetSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除短剧项目资产
	zyAssetDelete(data) {
		return request('delete', data)
	},
	// 获取短剧项目资产详情
	zyAssetDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载短剧项目资产导入模板
    zyAssetDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入短剧项目资产
	zyAssetImport(data) {
		return request('importData', data)
	},
	// 导出短剧项目资产
	zyAssetExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
