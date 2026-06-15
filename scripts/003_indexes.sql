---------- time_series ----------
CREATE INDEX idx_time_series_visibility ON garch.time_series (visibility);
CREATE INDEX idx_time_series_user_id ON garch.time_series (user_id);
CREATE INDEX idx_time_series_value_series_order ON garch.time_series_value (time_series_id, order_no);
---------- configuration ----------
CREATE INDEX idx_configuration_user_id ON garch.configuration (user_id);
---------- GARCH model ----------
CREATE INDEX idx_garch_model_configuration_id ON garch.garch_model (configuration_id);
---------- run_alpha ----------
CREATE INDEX idx_run_alpha_calculation_order ON garch.run_alpha (calculation_id, order_no);
---------- run_beta ----------
CREATE INDEX idx_run_beta_calculation_order ON garch.run_beta (calculation_id, order_no);
---------- model_alpha ----------
CREATE INDEX idx_model_alpha_model_order ON garch.model_alpha (model_id, order_no);
---------- model_beta ----------
CREATE INDEX idx_model_beta_model_order ON garch.model_beta (model_id, order_no);
