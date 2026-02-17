package com.example.garchapplication.service;

import com.example.garchapplication.mapper.GarchModelMapper;
import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.GarchModel;
import com.example.garchapplication.model.entity.ModelShockWeight;
import com.example.garchapplication.model.entity.ModelVarianceWeight;
import com.example.garchapplication.repository.GarchModelRepository;
import com.example.garchapplication.repository.ModelShockWeightRepository;
import com.example.garchapplication.repository.ModelVarianceWeightRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public GarchModel saveModel(GarchModelCalculationDTO garchModelCalculationDTO, Configuration configuration) {
        GarchModel garchModel = new GarchModel();
        garchModel.setConfiguration(configuration);
        garchModel.setName(garchModelCalculationDTO.name());
        garchModel.setStartVariance(garchModelCalculationDTO.startVariance());
        garchModel.setConstantVariance(garchModelCalculationDTO.constantVariance());
        garchModelRepository.save(garchModel);

        return garchModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveModelVarianceWeight(GarchModel garchModel, double value,  int index) {
        ModelVarianceWeight modelVarianceWeight = new ModelVarianceWeight();
        modelVarianceWeight.setGarchModel(garchModel);
        modelVarianceWeight.setOrderNo(index + 1);
        modelVarianceWeight.setValue(value);
        modelVarianceWeightRepository.save(modelVarianceWeight);
    }

    /**
    * {@inheritDoc}
     */
    @Override
    public void saveModelShockWeight(GarchModel garchModel, double value, int index) {
        ModelShockWeight modelShockWeight = new ModelShockWeight();
        modelShockWeight.setGarchModel(garchModel);
        modelShockWeight.setOrderNo(index + 1);
        modelShockWeight.setValue(value);
        modelShockWeightRepository.save(modelShockWeight);
    }
}
