package com.example.garchapplication.service;

import com.example.garchapplication.mapper.GarchModelMapper;
import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.GarchModel;
import com.example.garchapplication.model.entity.ModelShockWeight;
import com.example.garchapplication.model.entity.ModelVarianceWeight;
import com.example.garchapplication.model.enums.CellStyleNames;
import com.example.garchapplication.repository.GarchModelRepository;
import com.example.garchapplication.repository.ModelShockWeightRepository;
import com.example.garchapplication.repository.ModelVarianceWeightRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GarchModelServiceImpl implements GarchModelService {

    private final GarchModelRepository garchModelRepository;
    private final ModelVarianceWeightRepository modelVarianceWeightRepository;
    private final ModelShockWeightRepository modelShockWeightRepository;

    private static final int MODEL_ROWS = 5;

    @Autowired
    public GarchModelServiceImpl(GarchModelRepository garchModelRepository, ModelVarianceWeightRepository modelVarianceWeightRepository, ModelShockWeightRepository modelShockWeightRepository) {
        this.garchModelRepository = garchModelRepository;
        this.modelVarianceWeightRepository = modelVarianceWeightRepository;
        this.modelShockWeightRepository = modelShockWeightRepository;
    }

    @Override
    public List<GarchModel> findAllGarchModelsByConfigurationId(Long configurationId) {
        return garchModelRepository.findAllByConfigurationId(configurationId);
    }

    /**
    * {@inheritDoc}
     */
    @Override
    public GarchModelCalculationDTO extractGarchModelCalculationDTO(Long modelId) {
        GarchModel garchModel = garchModelRepository.findById(modelId).orElse(null);

        List<ModelVarianceWeight> modelVarianceWeightList = modelVarianceWeightRepository.findAllByGarchModelIdOrderByOrderNoAsc(garchModel.getId());
        List<ModelShockWeight> modelShockWeightList = modelShockWeightRepository.findAllByGarchModelIdOrderByOrderNoAsc(garchModel.getId());

        return GarchModelMapper.toGarchModelCalculationDTO(garchModel, modelVarianceWeightList, modelShockWeightList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GarchModelDTO extractGarchModelDTO(Long modelId){
        GarchModel garchModel = garchModelRepository.findById(modelId).orElse(null);

        List<ModelVarianceWeight> modelVarianceWeightList = modelVarianceWeightRepository.findAllByGarchModelIdOrderByOrderNoAsc(garchModel.getId());
        List<ModelShockWeight> modelShockWeightList = modelShockWeightRepository.findAllByGarchModelIdOrderByOrderNoAsc(garchModel.getId());

        return GarchModelMapper.toGarchModelDTO(garchModel, modelVarianceWeightList, modelShockWeightList);
    }

    @Override
    public List<GarchModelDTO> extractGarchModelDTOsByConfigurationId(Long configurationId) {
        List<GarchModel> garchModelList = findAllGarchModelsByConfigurationId(configurationId);
        List<GarchModelDTO> garchModelDTOList = new ArrayList<>();
        garchModelList.forEach(garchModel -> {
            garchModelDTOList.add(extractGarchModelDTO(garchModel.getId()));
        });
        return garchModelDTOList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GarchModelCalculationDTO> extractGarchModelsFromFileSheet(Sheet sheet) {
        List<GarchModelCalculationDTO> garchModelCalculationDTOS = new ArrayList<>();

        for (int i = 1; i < sheet.getLastRowNum(); i += MODEL_ROWS) {
            String modelName = "";
            double startVariance = 0;
            double constantVariance = 0;
            List<Double> lastVariances = new ArrayList<>();
            List<Double> lastShocks = new ArrayList<>();
            for (int j = i; j < i + MODEL_ROWS; j++) {
                Row row = sheet.getRow(j);
                switch (j % MODEL_ROWS) {
                    case 0 -> {
                        for (int currentCellNum = 1; currentCellNum < row.getLastCellNum(); currentCellNum++) {
                            lastShocks.add(row.getCell(currentCellNum).getNumericCellValue());
                        }
                    }
                    case 1 -> modelName = row.getCell(1).getStringCellValue();
                    case 2 -> startVariance = row.getCell(1).getNumericCellValue();
                    case 3 -> constantVariance = row.getCell(1).getNumericCellValue();
                    case 4 -> {
                        for (int currentCellNum = 1; currentCellNum < row.getLastCellNum(); currentCellNum++) {
                            lastVariances.add(row.getCell(currentCellNum).getNumericCellValue());
                        }
                    }
                }
            }
            garchModelCalculationDTOS.add(new GarchModelCalculationDTO(modelName, startVariance, constantVariance, lastVariances, lastShocks));
        }

        return garchModelCalculationDTOS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveModel(GarchModelCalculationDTO garchModelCalculationDTO, Configuration configuration) {
        GarchModel garchModel = new GarchModel();
        garchModel.setConfiguration(configuration);
        garchModel.setName(garchModelCalculationDTO.name());
        garchModel.setStartVariance(garchModelCalculationDTO.startVariance());
        garchModel.setConstantVariance(garchModelCalculationDTO.constantVariance());
        garchModelRepository.save(garchModel);

        for(int i = 0; i < garchModelCalculationDTO.lastVariances().size(); i++){
            double value = garchModelCalculationDTO.lastVariances().get(i);
            saveModelVarianceWeight(garchModel, value, i);
        }

        for(int i = 0; i < garchModelCalculationDTO.lastShocks().size(); i++){
            double value = garchModelCalculationDTO.lastShocks().get(i);
            saveModelShockWeight(garchModel, value, i);
        }
    }

    /**
     * Adds variance weight of the given GARCH model to database.
     *
     * @param garchModel GARCH model that variance weight belongs to
     * @param value value of the variance weight
     * @param index order of the variance weight
     */
    private void saveModelVarianceWeight(GarchModel garchModel, double value,  int index) {
        ModelVarianceWeight modelVarianceWeight = new ModelVarianceWeight();
        modelVarianceWeight.setGarchModel(garchModel);
        modelVarianceWeight.setOrderNo(index + 1);
        modelVarianceWeight.setValue(value);
        modelVarianceWeightRepository.save(modelVarianceWeight);
    }

    /**
     * Adds shock weight of the given GARCH model to database.
     *
     * @param garchModel GARCH model that shock weight belongs to
     * @param value value of the shock weight
     * @param index order of the shock weight
     */
    private void saveModelShockWeight(GarchModel garchModel, double value, int index) {
        ModelShockWeight modelShockWeight = new ModelShockWeight();
        modelShockWeight.setGarchModel(garchModel);
        modelShockWeight.setOrderNo(index + 1);
        modelShockWeight.setValue(value);
        modelShockWeightRepository.save(modelShockWeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGarchModel(long modelId, GarchModelCalculationDTO garchModelCalculationDTO) {
        GarchModel garchModel = garchModelRepository.findById(modelId).orElseThrow(() -> new RuntimeException("GarchModel not found"));
        garchModel.setName(garchModelCalculationDTO.name());
        garchModel.setStartVariance(garchModelCalculationDTO.startVariance());
        garchModel.setConstantVariance(garchModelCalculationDTO.constantVariance());

        List<ModelVarianceWeight> modelVarianceWeightList =  modelVarianceWeightRepository.findAllByGarchModelIdOrderByOrderNoAsc(modelId);
        updateVarianceWeights(garchModel, modelVarianceWeightList, garchModelCalculationDTO.lastVariances());

        List<ModelShockWeight> modelShockWeightList = modelShockWeightRepository.findAllByGarchModelIdOrderByOrderNoAsc(modelId);
        updateShockWeights(garchModel, modelShockWeightList, garchModelCalculationDTO.lastShocks());
    }


    private void updateVarianceWeights(GarchModel garchModel, List<ModelVarianceWeight> existingValues, List<Double> newValues) {
        int common = Math.min(existingValues.size(), newValues.size());

        for (int i = 0; i < common; i++) {
            existingValues.get(i).setValue(newValues.get(i));
        }

        if (newValues.size() > existingValues.size()) {
            for (int i = existingValues.size(); i < newValues.size(); i++) {
                saveModelVarianceWeight(garchModel, newValues.get(i), i);
            }
        }

        if (existingValues.size() > newValues.size()) {
            List<ModelVarianceWeight> extra = existingValues.subList(newValues.size(), existingValues.size());
            modelVarianceWeightRepository.deleteAll(extra);
        }
    }

    private void updateShockWeights(GarchModel garchModel, List<ModelShockWeight> existingValues, List<Double> newValues) {
        int common = Math.min(existingValues.size(), newValues.size());

        for (int i = 0; i < common; i++) {
            existingValues.get(i).setValue(newValues.get(i));
        }

        if (newValues.size() > existingValues.size()) {
            for (int i = existingValues.size(); i < newValues.size(); i++) {
                saveModelShockWeight(garchModel, newValues.get(i), i);
            }
        }

        if (existingValues.size() > newValues.size()) {
            List<ModelShockWeight> extra = existingValues.subList(newValues.size(), existingValues.size());
            modelShockWeightRepository.deleteAll(extra);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGarchModel(Long modelId){
        garchModelRepository.deleteById(modelId);
    }

    @Override
    public int addGarchModelToSheet(GarchModelDTO garchModelDTO, Sheet sheet, int index, Map<String, CellStyle> styles) {
        Row row = sheet.createRow(index++);
        Cell cell = row.createCell(0);
        cell.setCellValue("Název modelu:");
        cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
        cell = row.createCell(1);
        cell.setCellValue(garchModelDTO.name());
        cell.setCellStyle(styles.get(CellStyleNames.MODEL_NAME.toString()));
        sheet.addMergedRegion(new CellRangeAddress(index-1, index-1, 1, 8));

        row = sheet.createRow(index++);
        cell = row.createCell(0);
        cell.setCellValue("Počáteční rozptyl:");
        cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
        row.createCell(1).setCellValue(garchModelDTO.startVariance());

        row = sheet.createRow(index++);
        cell = row.createCell(0);
        cell.setCellValue("Konstantní rozptyl:");
        cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
        row.createCell(1).setCellValue(garchModelDTO.constantVariance());

        row = sheet.createRow(index++);
        cell = row.createCell(0);
        cell.setCellValue("Váhy minulých rozptylů:");
        cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
        for(int i = 0; i < garchModelDTO.lastVariances().size(); i++) {
            double value = garchModelDTO.lastVariances().get(i);
            row.createCell(i + 1).setCellValue(value);
        }

        row = sheet.createRow(index++);
        cell = row.createCell(0);
        cell.setCellValue("Váhy minulých odhadů:");
        cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
        for(int i = 0; i < garchModelDTO.lastShocks().size(); i++) {
            double value = garchModelDTO.lastShocks().get(i);
            row.createCell(i + 1).setCellValue(value);
        }
        return index;
    }
}
