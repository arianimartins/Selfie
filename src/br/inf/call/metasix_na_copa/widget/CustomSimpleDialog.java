package br.inf.call.metasix_na_copa.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import br.inf.call.metasix_na_copa.R;

public class CustomSimpleDialog extends Dialog {
	private int textoBotaoOk;
	private int textoTitulo;
	private int textoDescricao;
	private Button ok;
	private Button cancel;
	private boolean contemCancel;
	private Integer idImagem;
	private Context context;
	private boolean progress;
	
	/**
	 * Construtor para definir Dialog Comum
	 * @param context
	 * @param textoBotaoOk
	 * @param contemCancel
	 * @param textoTitulo
	 * @param textoDescricao
	 */
	public CustomSimpleDialog(Context context,int textoBotaoOk, boolean contemCancel,int textoTitulo, int textoDescricao){
		super(context);
		this.context = context;
		this.textoBotaoOk = textoBotaoOk;
		this.textoTitulo = textoTitulo;
		this.textoDescricao = textoDescricao;
		this.contemCancel = contemCancel;
		configure();
	}
	
	/**
	 * Construtor para definir Dialog com Imagem
	 * @param context
	 * @param textoBotaoOk
	 * @param contemCancel
	 * @param textoTitulo
	 * @param textoDescricao
	 * @param idImagem
	 */
	public CustomSimpleDialog(Context context, int textoBotaoOk, boolean contemCancel, int textoTitulo, int textoDescricao, Integer idImagem){
		super(context);
		this.context = context;
		this.textoBotaoOk = textoBotaoOk;
		this.textoTitulo = textoTitulo;
		this.textoDescricao = textoDescricao;
		this.contemCancel = contemCancel;
		this.idImagem = idImagem;
		configure();
	}
	
	/**
	 * Construtor para definir Dialog com Progress
	 * @param context
	 * @param textoTitulo
	 * @param textoDescricao
	 */
	public CustomSimpleDialog(Context context, int textoTitulo, int textoDescricao, boolean progress){
		super(context);
		this.context = context;
		this.textoTitulo = textoTitulo;
		this.textoDescricao = textoDescricao;
		this.progress = progress;
		configureProgress();
	}
	
	private void configureProgress(){
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		this.setContentView(R.layout.simple_customdialog_progress);
		carregarComponentesProgress();
	}
	
	private void carregarComponentesProgress(){
		String titulo = context.getResources().getString(textoTitulo);
		String descricao = context.getResources().getString(textoDescricao);
		((TextView) this.findViewById(R.id.simple_dialog_titulo_progress)).setText(titulo);
		((TextView) this.findViewById(R.id.simple_dialog_descricao_progress)).setText(descricao);
		if(!progress)
			((ProgressBar)this.findViewById(R.id.simple_dialog_progress)).setVisibility(ProgressBar.GONE);
	}
	
	private void configure(){
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		this.setContentView(R.layout.simple_customdialog);
		carregarComponentes();
	}
	
	private void carregarComponentes(){
		String titulo = context.getResources().getString(textoTitulo);
		String descricao = context.getResources().getString(textoDescricao);
		((TextView) this.findViewById(R.id.simple_dialog_titulo)).setText(titulo);
		((TextView) this.findViewById(R.id.simple_dialog_descricao)).setText(descricao);
		configuraBotoes();
		if(idImagem != null){
			ImageView image = (ImageView) this.findViewById(R.id.pop_icon);
			image.setImageResource(idImagem);
			image.setVisibility(ImageView.VISIBLE);
			
		}
	}

	private void configuraBotoes() {
		String textoBotao = context.getResources().getString(textoBotaoOk);
		ok = (Button) this.findViewById(R.id.simple_dialog_button_ok);
		ok.setText(textoBotao);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomSimpleDialog.this.dismiss();
			}
		});
		
		if(contemCancel){
			String cancelar = context.getResources().getString(R.string.txt_cancelar);
			cancel = (Button) this.findViewById(R.id.simple_dialog_button_cancel);
			cancel.setVisibility(Button.VISIBLE);
			cancel.setText(cancelar);
			setOnCancelClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CustomSimpleDialog.this.dismiss();
				}
			});
		}
	}
	
	/**
	 * Estabelece o comportamente do botão cancel
	 * @param onClickListener
	 */
	public void setOnCancelClickListener(View.OnClickListener onClickListener){
		if(contemCancel)
			cancel.setOnClickListener(onClickListener);
	}
	
	public void setOnOkClickListener(View.OnClickListener onClickListener){
		ok.setOnClickListener(onClickListener);
	}
}
